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
import game.ecs.Component;
import game.ecs.ECSystem;

public class Game {
	public static void main(String[] args) { // Everything might be a bit over engineered...
		GameLoop.init();
		GameLoop.setPostProcessShader(new Shader("resources/bloom.frag"));

		loadLevel();

		GameLoop.runBlocking();
		// System.exit(0);
	}
	
	public static void loadLevel() {
		GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screen().divide(2), 1)
		));

		GameLoop.track(GameLoop.getMainCameraEntity());

		GameLoop.track(Background.makeEntity());

		GameLoop.track(EnemySpawner.makeEntity());

		GameLoop.track(Player.makeEntity());
		// GameLoop.track(Enemy.makeEntity(new Vec2(100, 100)));
	}
	
}
