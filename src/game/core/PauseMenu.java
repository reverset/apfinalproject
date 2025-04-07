package game.core;

import java.util.ArrayList;

import com.raylib.Raylib;

import game.BetterButton;
import game.Color;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class PauseMenu {
    public static void open() {
        final var e = makeEntity();
        GameLoop.safeTrack(e);
    }

    public static Entity makeEntity() {
        return new Entity("Pause Menu")
            .register(new ECSystem() {
                private final ArrayList<Entity> pauseMenuEntities = new ArrayList<>();

                @Override
                public void setup() {}
                
                @Override
                public void frame() {
                    if (Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) {
                        GameLoop.defer(() -> {
                            GameLoop.destroy(entity);
                            GameLoop.unpause();
                        });
                    }
                }

                @Override
                public void ready() {
                    pauseMenuEntities.add(makeExitButton(entity));
                    pauseMenuEntities.add(makeMainMenuButton(entity));

                    pauseMenuEntities.forEach(p -> p.setPauseBehavior(true));
                    GameLoop.pause();
                }

                @Override
                public void destroy() {
                    pauseMenuEntities.forEach(GameLoop::destroy);
                    GameLoop.unpause();
                }

            }).setPauseBehavior(true);
    }

    private static Entity makeMainMenuButton(Entity main) {
        final var mainButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        mainButton.onClick.listenOnce(n -> {
            GameLoop.safeDestroy(main);
            GameLoop.defer(() -> {
                MainMenu.clearAndLoad();
            });
        });

        mainButton
            .setText("Main Menu")
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize();

        return GameLoop.track(new Entity("mainMenuButton")
            .addComponent(new Transform(Vec2.screenCenter().add(0, 100)))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(mainButton));
    }

    private static Entity makeExitButton(Entity main) {
        final var exitButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        exitButton.onClick.listenOnce(n -> {
            GameLoop.safeDestroy(main);
            GameLoop.defer(() -> {
                GameLoop.unpause();
                GameLoop.quit();
            });
        });

        exitButton
            .setText("Exit")
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize();

        return GameLoop.track(new Entity("exitButton")
            .addComponent(new Transform(Vec2.screenCenter().add(0, 200)))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(exitButton));
    }
}
