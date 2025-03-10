package game.core;

import game.Button;
import game.Camera;
import game.CameraSettings;
import game.Color;
import game.Game;
import game.GameLoop;
import game.Shader;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class MainMenu {
    public static void clearAndLoad() {
        GameLoop.getPostProcessShader().ifPresent(Shader::reset);
        GameLoop.clearAllEntitiesNow();
        
        GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screenCenter(), 1)
		));

		GameLoop.track(GameLoop.getMainCameraEntity());
		GameLoop.track(Background.makeEntity());
        
        final var startButton = new Button(() -> {
            GameLoop.clearAllEntities();
            GameLoop.defer(() -> {
                Game.loadLevel();
            });
        });
        GameLoop.track(new Entity("startButton")
            .addComponent(new Transform(Vec2.screenCenter()))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(new RectRender())
            .register(startButton)
        );
    }
}
