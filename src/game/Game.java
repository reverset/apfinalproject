package game;

import com.raylib.Raylib;

import game.ecs.Entity;
import game.ecs.comps.Transform;
import game.core.Background;
import game.core.CircleEnemy;
import game.core.Enemy;
import game.core.EnemySpawner;
import game.core.Physics;
import game.core.Player;
import game.core.Tangible;
import game.core.rendering.Circle;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.Component;
import game.ecs.ECSystem;

public class Game {
	public static void main(String[] args) { // Everything might be a bit over engineered...
		GameLoop.init();

		Shader post = new Shader("resources/post.frag");
		post.setResetFunction(() -> {
			post.setShaderValue("vignetteStrength", 0f);
		});

		GameLoop.setPostProcessShader(post);

		loadLevel();

		GameLoop.runBlocking();
		// System.exit(0);
	}
	
	public static void loadLevel() { // make abstraction to handle levels TODO
		GameLoop.getPostProcessShader().ifPresent(Shader::reset);
		GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screenCenter(), 1)
		));

		GameLoop.track(GameLoop.getMainCameraEntity());

		GameLoop.track(Background.makeEntity());

		GameLoop.track(EnemySpawner.makeEntity());

		GameLoop.track(Player.makeEntity());
	}
	
}
