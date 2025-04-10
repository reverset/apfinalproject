package game.core;

import java.util.ArrayList;

import com.raylib.Raylib;

import game.BetterButton;
import game.Camera;
import game.CameraSettings;
import game.Color;
import game.Game;
import game.GameLoop;
import game.Shader;
import game.Text;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class MainMenu { // not a fan of this implementation, but I didn't feel like writing a proper implementation for levels/scenes.
    private static final ArrayList<Entity> menuItems = new ArrayList<>();

    public static void clearAndLoad() {
        menuItems.clear();
        GameLoop.getPostProcessShader().ifPresent(Shader::reset);
        GameLoop.clearAllEntitiesNow();

        final var creditsEntity = makeCreditsInfo();
        creditsEntity.onVisibilityChange.listen(v -> {
            if (v) {
                menuItems.forEach(Entity::show);
            }
        });
        creditsEntity.hide();
        GameLoop.safeTrack(creditsEntity);
        
        GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screenCenter(), 1)
		));

		GameLoop.track(GameLoop.getMainCameraEntity());
		GameLoop.track(Background.makeEntity());

        
        makeButton("Play", 0, () -> {
            GameLoop.clearAllEntities();
            GameLoop.defer(() -> {
                Game.loadLevel();
            });
        });

        makeButton("Exit", 200, () -> {
            GameLoop.quit();
        });

        makeButton("Credits", 100, () -> {
            menuItems.forEach(Entity::hide);
            creditsEntity.show();
        });

        makeTitle();
    }

    private static Entity makeButton(String text, float yOffset, Runnable action) {
        final var button = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        button
            .setText(text)
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize()
            .onClick.listenOnce(n -> action.run());
        
        final var e = GameLoop.track(new Entity(text+"::button")
            .addComponent(new Transform(Vec2.screenCenter().add(0, yOffset)))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(button));
        menuItems.add(e);
        return e;
    }

    private static Entity makeTitle() {
        final var title = GameLoop.track(new Entity("title")
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
        menuItems.add(title);
        return title;
    }

    private static Entity makeCreditsInfo() {
        Entity e = new Entity("credits");
        e.register(new ECSystem() {
            private final Text text = new Text("N/A", new Vec2(), 54, Color.WHITE);

            @Override
            public void setup() {
                text.text = "\n\n\t\tCredits\n\n\t\t" +
                    "Programming \t\t\t\tSebastian\n\t\t" +
                    "Game Design \t\t\t\tSebastian";
            }

            @Override
            public void frame() {
                if (!entity.isHidden()) {
                    if (Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) {
                        entity.hide();
                    }
                }
            }

            @Override
            public void hudRender() {
                text.renderWithNewlines();
            }
        });
        return e;
    }
}
