package game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import game.Tween.TweenFunction;
import game.ecs.Entity;

public class GameLoop {
	public static final int SCREEN_WIDTH = 1280; // This width and height are actually the render size.
	public static final int SCREEN_HEIGHT = 720;

	public static final boolean LOG_ERR_TO_FILE = true;

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

	private static Stopwatch infrequentUpdateStopwatch = Stopwatch.ofRealTime();

	private static final PollingIterator<Runnable> deferIterator = new PollingIterator<>(deferments);

	private static final ResourceManager resourceManager = new ResourceManager();

	private static final Vec2 worldMouseVec = new Vec2();
	private static final Vec2 mouseVec = new Vec2();

	private static int exceptions = 0;

	private static boolean paused = false;

	private static double unpausedTime = 0;

	public static ResourceManager getResourceManager() {
		return resourceManager;
	}

	public static void togglePause() {
		paused = !paused;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void pause() {
		paused = true;
	}

	public static void unpause() {
		paused = false;
	}

	public static Camera getMainCamera() {
		return mainCameraSystem;
	}

	public static float getTime() {
		return (float) Raylib.GetTime();
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

	public static Optional<Shader> getPostProcessShader() {
		return Optional.ofNullable(postProcesShader);
	}

	public static void disablePostProcessShader() {
		postProcesShader = null;
	}

	public static void setMainCamera(Entity entity) {
		Objects.requireNonNull(entity);
		mainCamera = entity;
		mainCameraSystem = mainCamera.getSystem(Camera.class).orElseThrow(() -> new RuntimeException("Missing Camera system when setting a new Camera entity."));
	}
	
	public static <T extends Entity> T track(T entity) {
		Objects.requireNonNull(entity);
		entities.add(entity);
		entity.ready();
		return entity;
	}

	public static <T> Tween<T> makeTween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
		return makeTween(supplier, durationSeconds, updater, Raylib::GetTime);
	}

	public static <T> Tween<T> makeTweenGameTime(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
		return makeTween(supplier, durationSeconds, updater, GameLoop::getUnpausedTime);
	}

	public static <T> Tween<T> makeTween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater, DoubleSupplier timeSupp) {
		Objects.requireNonNull(supplier);
		Objects.requireNonNull(updater);
		Objects.requireNonNull(timeSupp);
		var tween = new Tween<>(supplier, durationSeconds, updater, timeSupp);
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
		Objects.requireNonNull(entity);
		entity.destroy();
		entities.remove(entity);
	}

	public static void safeDestroy(Entity entity) {
		Objects.requireNonNull(entity);
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
		Objects.requireNonNull(action);
		deferments.add(action);
	}

	public static void schedule(ScheduledAction action) {
		Objects.requireNonNull(action);
		scheduledActions.add(action);
	}

	public static void runAfter(Entity entity, Duration duration, Runnable action) {
		Objects.requireNonNull(action);
		Stopwatch stopwatch = Stopwatch.ofRealTime();
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

	public static void clearAllEntitiesNow() {
		ListIterator<Entity> iter = entities.listIterator();
		while (iter.hasNext()) {
			Entity entity = iter.next();
			entity.destroy();
			iter.remove();
		}
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
				try {
					synchronized (entities) {
						if (iter.hasNext()) {
							Entity entity = iter.next();
							entity.destroy();
							iter.remove();
						} else break;
					}
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
					synchronized (entities) {
						entities.clear();
					}
					break;
				}
			}
		});
		thread.setName("Entity Clearer");
		thread.start();
	}
	
	public static void init() {
		Raylib.SetConfigFlags(Raylib.FLAG_VSYNC_HINT | Raylib.FLAG_WINDOW_ALWAYS_RUN | Raylib.FLAG_WINDOW_RESIZABLE);
		Raylib.SetTraceLogLevel(Raylib.LOG_WARNING);
		Raylib.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Game");
		Raylib.SetExitKey(0);

		renderTexture = Raylib.LoadRenderTexture(SCREEN_WIDTH, SCREEN_HEIGHT);
		Raylib.SetTextureFilter(renderTexture.texture(), Raylib.TEXTURE_FILTER_POINT);

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
		try {
			runBlockingActual();
		} catch (Exception e) {
			if (LOG_ERR_TO_FILE) {
				StringWriter stringWriter = new StringWriter();
				e.printStackTrace(new PrintWriter(stringWriter));
				String errmsg = stringWriter.toString();
	
				File errFile = new File("./err.stacktrace");
				if (!errFile.exists()) {
					try {
						errFile.createNewFile();
					} catch (IOException ioe) {
						ioe.printStackTrace();
						System.out.println("Failed to create err file.");
					}
				}
	
				try {
					FileWriter writer = new FileWriter(errFile);
					writer.write(errmsg);
					writer.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

			throw e;
		}
	}

	private static void runBlockingActual() {
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
			forEachEntitySafe(Entity::infrequentUpdate, false);
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
		resourceManager.unloadAll();

		System.gc();
		manageCleanupQueue();
		if (Raylib.IsWindowReady()) {			
			Raylib.CloseWindow();
		}
	}

	public static Vec2 getMousePosition() {
		return worldMouseVec.setEq(Raylib.GetMouseX(), Raylib.GetMouseY()).screenToWorldEq();
	}

	public static Vec2 getMouseScreenPosition() {
		return mouseVec.setEq(Raylib.GetMouseX(), Raylib.GetMouseY()).toVirtualScreenEq();
	}

	private static void forEachEntitySafe(Consumer<Entity> action, boolean ignorePause) {
		ListIterator<Entity> iter = entities.listIterator();
		while (iter.hasNext()) {
			Entity en = iter.next();
			if (!ignorePause && !en.runWhilePaused && paused) continue;
			try {
				action.accept(en);
			} catch (RecoverableException e) {
				e.printStackTrace();
				System.err.println("Caused by entity '" + en.name + "'");
				GameLoop.safeDestroy(en);
				exceptions += 1;
			}
		}
	}
	
	private static void frameUpdate() {
        // if (Raylib.IsKeyPressed(Raylib.KEY_P)) GameLoop.togglePause(); // for testing
		if (!GameLoop.isPaused()) unpausedTime += Raylib.GetFrameTime();
		forEachEntitySafe(Entity::frame, false);
	}
	
	private static void renderUpdate() {
		Raylib.BeginTextureMode(renderTexture);

		Raylib.ClearBackground(Jaylib.BLACK);
		
		Raylib.BeginMode2D(mainCameraSystem.getPointer());
		
		// getEntitiesIterator().forEachRemaining(Entity::render);
		forEachEntitySafe(Entity::render, true);
		
		Raylib.EndMode2D();
		
		hudRender();
		Raylib.DrawFPS(15, 15);
		
		Raylib.EndTextureMode();
		
		Raylib.BeginDrawing();
		Raylib.ClearBackground(Jaylib.BLACK);
		
		if (postProcesShader != null) postProcesShader.activate();
		
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
			Raylib.Vector2Zero(),
			0,
			Color.WHITE.getPointer()
		);


		if (postProcesShader != null) postProcesShader.deactivate();
		Raylib.EndDrawing();
	}

	private static void hudRender() {
		forEachEntitySafe(Entity::hudRender, true);

		if (exceptions > 0) {
			renderError();
		}
	}

	private static void renderError() {
		Raylib.DrawRectangle(SCREEN_WIDTH - 200, 15, 200, 50, Color.RED.getPointerNoUpdate());
		Raylib.DrawText("Check console.", SCREEN_WIDTH - 200, 15, 24, Color.WHITE.getPointerNoUpdate());
		Raylib.DrawText("errors: #" + exceptions, SCREEN_WIDTH-200, 39, 24, Color.WHITE.getPointerNoUpdate());
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

	public static double getUnpausedTime() {
		return unpausedTime;
	}
}
