package game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import game.Tween.TweenFunction;
import game.ecs.Entity;

public class GameLoop {
	public static final int SCREEN_WIDTH = 1280; // This width and height are actually the render size.
	public static final int SCREEN_HEIGHT = 720;

	public static final float INFREQUENT_UPDATE_RATE = 1f / 32;
	public static float timeScale = 1f;

	public static final Signal<Duration> onShutdown = new Signal<>();

	private static final ArrayList<Entity> entities = new ArrayList<>();
	private static final Queue<Runnable> deferments = new LinkedList<>();
	private static final LinkedList<ScheduledAction> scheduledActions = new LinkedList<>();

	private static Entity mainCamera;
	private static Camera mainCameraSystem;

	private static Raylib.RenderTexture renderTexture;
	private static Raylib.Rectangle screenRect;
	private static Raylib.Rectangle scaledScreenRect;

	private static Shader postProcesShader = null;

	private static boolean shouldShutdown = false;

	private static Stopwatch infrequentUpdateStopwatch = new Stopwatch();

	private static final PollingIterator<Runnable> deferIterator = new PollingIterator<>(deferments);

	public static Camera getMainCamera() {
		return mainCameraSystem;
	}

	public static int entityCount() {
		return entities.size();
	}

	public static Entity getMainCameraEntity() {
		return mainCamera;
	}

	public static void setPostProcessShader(Shader shader) {
		postProcesShader = shader;
	}

	public static void disablePostProcessShader() {
		postProcesShader = null;
	}

	public static void setMainCamera(Entity entity) {
		mainCamera = entity;
		mainCameraSystem = mainCamera.getSystem(Camera.class).orElseThrow(() -> new RuntimeException("Missing Camera system when setting a new Camera entity."));
	}
	
	public static <T extends Entity> T track(T entity) {
		entities.add(entity);
		entity.ready();
		return entity;
	}

	public static <T> Tween<T> makeTween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
		var tween = new Tween<>(supplier, durationSeconds, updater);
		Entity entity = new Entity("tween")
			.register(tween);
		
		GameLoop.safeTrack(entity);

		return tween;
	}

	public static <T extends Entity> T safeTrack(T entity) {
		GameLoop.defer(() -> {
			GameLoop.track(entity);
		});
		return entity;
	}

	public static boolean isPresent(Entity entity) {
		return entities.contains(entity);
	}

	public static void destroy(Entity entity) {
		entity.destroy();
		entities.remove(entity);
	}

	public static void safeDestroy(Entity entity) {
		GameLoop.defer(() -> {
			if (entities.contains(entity)) {
				destroy(entity);
			}
		});
	}
	
	public static Iterator<Entity> getEntitiesIterator() {
		return entities.iterator();
	}

	public static Optional<Entity> findEntityByTag(Object tag) {
		return entities.stream()
			.filter(e -> e.hasTag(tag))
			.findFirst();
	}

	public static List<Entity> findEntitiesByTag(Object tag) {
		return entities.stream()
			.filter(e -> e.hasTag(tag))
			.toList();
	}

	public static void defer(Runnable action) {
		deferments.add(action);
	}

	public static void schedule(ScheduledAction action) {
		scheduledActions.add(action);
	}

	public static void runAfter(Entity entity, Duration duration, Runnable action) {
		Stopwatch stopwatch = new Stopwatch();
		var toSchedule = new ScheduledAction(entity, List.of(
			() -> stopwatch.hasElapsed(duration),
			() -> {
				action.run();
				return true;
			}
			));

		stopwatch.start();
		GameLoop.schedule(toSchedule);
	}

	public static void clearAllEntities() {
		GameLoop.defer(() -> {
			ListIterator<Entity> iter = entities.listIterator();
			while (iter.hasNext()) {
				Entity entity = iter.next();
				entity.destroy();
				iter.remove();
			}
		});
	}

	public static void clearAllEntitiesAsync() {
		Thread thread = new Thread(() -> {
			ListIterator<Entity> iter = null;
			synchronized (entities) {
				iter = entities.listIterator();
			}
			
			while (true) { // this is ugly yes, but I did it so that the thread doesn't hold the lock on entities, so that I can update a progress bar.
				synchronized (entities) {
					if (iter.hasNext()) {
						Entity entity = iter.next();
						entity.destroy();
						iter.remove();
					} else break;
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {}
			}
		});
		thread.setName("Entity Clearer");
		thread.start();
	}
	
	public static void init() {
		Raylib.SetConfigFlags(Raylib.FLAG_VSYNC_HINT);
		Raylib.SetConfigFlags(Raylib.FLAG_WINDOW_RESIZABLE);
		Raylib.SetTraceLogLevel(Raylib.LOG_WARNING);
		Raylib.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Game");

		renderTexture = Raylib.LoadRenderTexture(SCREEN_WIDTH, SCREEN_HEIGHT);
		Raylib.SetTextureFilter(renderTexture.texture(), Raylib.TEXTURE_FILTER_BILINEAR);

		screenRect = new Raylib.Rectangle().x(0).y(0).width(renderTexture.texture().width()).height(-renderTexture.texture().height());

		float scale = getScreenTextureScale();
		scaledScreenRect = new Raylib.Rectangle()
			.x((Raylib.GetScreenWidth() - (SCREEN_WIDTH * scale))*0.5f)
			.y((Raylib.GetScreenHeight() - (SCREEN_HEIGHT * scale))*0.5f)
			.width(SCREEN_WIDTH * scale)
			.height(SCREEN_HEIGHT * scale);
	}
	
	public static void quit() {
		shouldShutdown = true;
	}
	
	public static void runBlocking() {
		while (!Raylib.WindowShouldClose() && !shouldShutdown) {
			frameUpdate();
			infrequentUpdate();
			renderUpdate();
			updateSchedule();
			runAllDeferred();
			manageCleanupQueue();
		}
		deinit();
	}

	public static void infrequentUpdate() {
		if (infrequentUpdateStopwatch.hasElapsedSecondsAdvance(INFREQUENT_UPDATE_RATE)) {
			getEntitiesIterator().forEachRemaining(Entity::infrequentUpdate);
		}
	}
	
	public static void deinit() {
		onShutdown.emit(Duration.ofMillis((long) (Raylib.GetTime()*1_000)));

		int entitiesToClear = entities.size();
		int entitiesLeft = entitiesToClear;
		clearAllEntitiesAsync();
		while (true) {
			synchronized (entities) {
				int size = entities.size();
				entitiesLeft = entitiesToClear-size;
				if (size <= 0) break;
			}

			Raylib.BeginDrawing();
			Raylib.ClearBackground(Color.BLACK.getPointer());
			
			long percentage = Math.round((entitiesLeft/(double)entitiesToClear)*100);
			Raylib.DrawText("Closing game ... (This may take some time) " + percentage + "%", 15, 15, 24, Color.WHITE.getPointer());

			Raylib.EndDrawing();

		}

		System.gc();
		manageCleanupQueue();
		if (Raylib.IsWindowReady()) {			
			Raylib.CloseWindow();
		}
	}

	public static Vec2 getMousePosition() {
		return new Vec2(
			Raylib.GetMouseX(),
			Raylib.GetMouseY()
		).screenToWorldEq();
	}
	
	private static void frameUpdate() {
		getEntitiesIterator().forEachRemaining(Entity::frame);
	}
	
	private static void renderUpdate() {
		Raylib.BeginTextureMode(renderTexture);

		Raylib.ClearBackground(Jaylib.BLACK);
		
		Raylib.BeginMode2D(mainCameraSystem.getPointer());
		
		getEntitiesIterator().forEachRemaining(Entity::render);
		
		Raylib.EndMode2D();
		
		hudRender();
		Raylib.DrawFPS(15, 15);
		
		Raylib.EndTextureMode();
		
		Raylib.BeginDrawing();

		if (postProcesShader != null) postProcesShader.activate();
		
		// Raylib.DrawTextureRec(renderTexture.texture(), screenRect, Vec2.zero().getPointer(), Jaylib.WHITE);
		float scale = getScreenTextureScale();

		scaledScreenRect
			.x((Raylib.GetScreenWidth() - (SCREEN_WIDTH * scale))*0.5f)
			.y((Raylib.GetScreenHeight() - (SCREEN_HEIGHT * scale))*0.5f)
			.width(SCREEN_WIDTH*scale)
			.height(SCREEN_HEIGHT*scale);

		Raylib.DrawTexturePro(
			renderTexture.texture(),
			screenRect,
			scaledScreenRect,
			Vec2.ZERO.getPointer(),
			0,
			Color.WHITE.getPointer()
		);


		if (postProcesShader != null) postProcesShader.deactivate();
		Raylib.EndDrawing();
	}

	private static void hudRender() {
		getEntitiesIterator().forEachRemaining(Entity::hudRender);
	}
	
	private static void manageCleanupQueue() {
		int i = 0;
		Runnable action = null;
		while ((action = Janitor.poll()) != null) {
			action.run();
			i += 1;

			if (i > 2_000) break; // I've seen up to 500K objects being queued. This is so stuff is cleaned over multiple frames to avoid stutters.
		}
	}

	private static void updateSchedule() {
		ListIterator<ScheduledAction> iter = scheduledActions.listIterator();
		
		while (iter.hasNext()) {
			ScheduledAction run = iter.next();
			if (run.update()) {
				iter.remove();
			}
		}
	}

	private static void runAllDeferred() {
		deferIterator.forEachRemaining(Runnable::run);
	}

	public static float getScreenTextureScale() {
		return Math.min(
			Raylib.GetScreenWidth() / (float) SCREEN_WIDTH,
			Raylib.GetScreenHeight() / (float) SCREEN_HEIGHT 
		);
	}
}
