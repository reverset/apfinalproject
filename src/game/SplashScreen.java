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
            .register(new TextureRenderer("resources/shapesinspacetitle.png", 3200/3, 1400/3)));

        Transform raylibTrans = raylib.getComponent(Transform.class).orElseGet(() -> new Transform());
        GameLoop.makeTween(Tween.overEase(1_000, 0, 0.8f), 1, val -> {
            raylibTrans.position.y = val;
        }).start();

        Text text = new Text("", new Vec2(40, 90), 54, Color.WHITE);
        GameLoop.track(Text.makeEntity(text));

        GameLoop.makeTween(Tween.reveal("I hope this is worthy of an A+."), 3, val -> {
            text.text = val;
        }).start();

        GameLoop.track(new Entity("tomainmenu")
            .register(new ECSystem() {
                private Stopwatch end = Stopwatch.ofRealTime();

                @Override
                public void setup() {
                }

                public void frame() {
                    if (end.hasElapsedSeconds(6) || Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) {
                        GameLoop.defer(() -> {
                            Shader post = new Shader("resources/post.frag");
                            post.setResetFunction(() -> {
                                post.setShaderValue("vignetteStrength", 0f);
                            });

                            GameLoop.setPostProcessShader(post);

                            MainMenu.clearAndLoad();
                        });
                    }
                }
                
            }));
    }
}
