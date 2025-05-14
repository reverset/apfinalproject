package game;

import com.raylib.Raylib;

import game.core.MainMenu;
import game.core.rendering.TextureRenderer;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class SplashScreen {
    public static void load() {
        GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screenCenter(), 1)
		));

        GameLoop.setBackgroundColor(Color.SPLASH_BLUE);

        Entity raylib = GameLoop.track(new Entity("splash-raylib")
            .addComponent(new Transform(new Vec2(0, 1000)))
            .register(new TextureRenderer("resources/raylib.png", 200, 200)));
        Transform raylibTrans = raylib.getComponent(Transform.class).orElseGet(() -> new Transform());
        GameLoop.makeTween(Tween.overEase(1_000, 0, 1), 2, val -> {
            raylibTrans.position.y = val;
        }).start();

        Text text = new Text("", new Vec2(50, 100), 24, Color.WHITE);
        GameLoop.track(Text.makeEntity(text));

        GameLoop.makeTween(Tween.reveal("Made with raylib. Game design and programming by Sebastian. I hope this is worth an A+ :)"), 4, val -> {
            text.text = val;
        }).start();

        GameLoop.track(new Entity("tomainmenu")
            .register(new ECSystem() {
                private Stopwatch end = Stopwatch.ofRealTime();

                @Override
                public void setup() {
                }

                public void frame() {
                    if (end.hasElapsedSeconds(10) || Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) {
                        GameLoop.defer(() -> {
                            MainMenu.clearAndLoad();
                        });
                    }
                }
                
            }));
    }
}
