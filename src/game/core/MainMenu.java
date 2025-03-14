package game.core;

import com.raylib.Raylib;

import game.BetterButton;
import game.Camera;
import game.CameraSettings;
import game.Color;
import game.Game;
import game.GameLoop;
import game.Shader;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
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
        
        final var startButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        startButton.onClick.listenOnce((n) -> {
            GameLoop.clearAllEntities();
            GameLoop.defer(() -> {
                Game.loadLevel();
            });
        });
        startButton
            .setText("Start")
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize();


        GameLoop.track(new Entity("startButton")
            .addComponent(new Transform(Vec2.screenCenter()))
            .addComponent(new Rect(200, 50, Color.WHITE))
            // .register(new RectRender().setHudMode(true))
            .register(startButton)
        );
        GameLoop.track(new Entity("title")
            .addComponent(new Transform(Vec2.screenCenter().minus(0, 200)))
            .register(new ECSystem() {
                private Transform trans;
                
                @Override
                public void setup() {
                    trans = require(Transform.class);
                }
                
                @Override
                public void hudRender() {
                    String text = "Shapes in Space";
                    int fontSize = 128;
                    int size = Raylib.MeasureText(text, fontSize);
                    Raylib.DrawText(text, trans.position.xInt()-size/2, trans.position.yInt(), fontSize, Color.WHITE.getPointerNoUpdate());
                }
            })
        );
    }
}
