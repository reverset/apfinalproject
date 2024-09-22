package game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Queue;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import game.ecs.Entity;
import game.ecs.comps.Transform;

public class GameLoop {
	public static final int SCREEN_WIDTH = 850;
	public static final int SCREEN_HEIGHT = 450;

	public static final Signal<Duration> onShutdown = new Signal<>();

	private static final ArrayList<Entity> entities = new ArrayList<>();
	private static final Queue<Runnable> deferments = new LinkedList<>();
	private static final LinkedList<ScheduledAction> scheduledActions = new LinkedList<>();

	private static Entity mainCamera;
	private static Camera mainCameraSystem;

	private static Raylib.RenderTexture renderTexture;
	private static Raylib.Rectangle screenRect;

	private static Shader postProcesShader = null;

	public static Camera getMainCamera() {
		return mainCameraSystem;
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
		Iterator<Entity> iter = getEntitiesIterator();
		
		while (iter.hasNext()) {
			Entity entity = iter.next();

			if (entity.hasTag(tag)) return Optional.of(entity);
		}

		return Optional.empty();
	}

	public static void defer(Runnable action) {
		deferments.add(action);
	}

	public static void schedule(ScheduledAction action) {
		scheduledActions.add(action);
	}

	public static void clearAllEntities() {
		GameLoop.defer(() -> {
			for (Entity entity : entities) {
				entity.destroy();
			}
			entities.clear();
		});
	}
	
	public static void init() {
		Raylib.SetConfigFlags(Raylib.FLAG_VSYNC_HINT);
		Raylib.SetTraceLogLevel(Raylib.LOG_WARNING);
		Raylib.InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Game");

		

		renderTexture = Raylib.LoadRenderTexture(SCREEN_WIDTH, SCREEN_HEIGHT);
		screenRect = new Raylib.Rectangle().x(0).y(0).width(SCREEN_WIDTH).height(-SCREEN_HEIGHT);
	}
	
	public static void quit() {
		Raylib.CloseWindow();
	}
	
	public static void runBlocking() {
		while (!Raylib.WindowShouldClose()) {
			frameUpdate();
			renderUpdate();
			updateSchedule();
			runAllDeferred();
			manageCleanupQueue();
		}
		deinit();
	}
	
	public static void deinit() {
		onShutdown.emit(Duration.ofMillis((long) (Raylib.GetTime()*1_000)));
		clearAllEntities();
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
		
		Raylib.DrawTextureRec(renderTexture.texture(), screenRect, Vec2.zero().getPointer(), Jaylib.WHITE);
		
		if (postProcesShader != null) postProcesShader.deactivate();
		Raylib.EndDrawing();
	}

	private static void hudRender() {
		getEntitiesIterator().forEachRemaining(Entity::hudRender);
	}
	
	private static void manageCleanupQueue() {
		Runnable action = null;
		while ((action = Janitor.poll()) != null) {
			action.run();
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
		new PollingIterator<>(deferments).forEachRemaining(Runnable::run);
	}
}
