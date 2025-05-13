package game;

import game.core.Background;
import game.core.EnemySpawner;
import game.core.MainMenu;
import game.core.Player;
import game.core.RandomPowerup;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Game {
	public static void main(String[] args) { // Everything might be a bit over engineered...
		GameLoop.init();

		Shader post = new Shader("resources/post.frag");
		post.setResetFunction(() -> {
			post.setShaderValue("vignetteStrength", 0f);
		});

		GameLoop.setPostProcessShader(post);

		// loadLevel();
		MainMenu.clearAndLoad();

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

		GameLoop.track(new Entity("a")
			.addComponent(new Transform())
			// .register(new Wobble(100, 10))
			.register(ParticlePresets.flame(Color.ORANGE)));

		RandomPowerup.showScreen();
		
	}
	
}
