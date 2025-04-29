package game.core;

import java.util.ArrayList;

import com.raylib.Raylib;

import game.BetterButton;
import game.Color;
import game.GameLoop;
import game.Text;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class PauseMenu {
    private static Entity pauseMenu = null;

    public static void open() {
        if (pauseMenu == null) pauseMenu = makeEntity();
        if (!GameLoop.isPresent(pauseMenu)) GameLoop.safeTrack(pauseMenu);

        GameLoop.defer(() -> pauseMenu.show());
        GameLoop.pause();
    }

    public static Entity makeEntity() {
        return new Entity("Pause Menu")
            .register(new ECSystem() {
                private final ArrayList<Entity> pauseMenuEntities = new ArrayList<>();
                private final Text text = new Text("PAUSED", Vec2.screenCenter(), 104, Color.WHITE);

                @Override
                public void setup() {
                    text.position.x -= text.measure() / 2;
                    text.position.y -= 50;

                    entity.onVisibilityChange.listen(v -> {
                        if (v) pauseMenuEntities.forEach(e -> e.hide());
                        else pauseMenuEntities.forEach(e -> e.show());
                    });
                }
                
                @Override
                public void frame() {
                    if (Raylib.IsKeyPressed(Raylib.KEY_ESCAPE) && entity.isVisible()) {
                        GameLoop.defer(() -> {
                            // GameLoop.destroy(entity);
                            entity.hide();
                            GameLoop.unpause();
                        });
                    }
                }

                @Override
                public void ready() {
                    pauseMenuEntities.add(makeExitButton(entity));
                    pauseMenuEntities.add(makeMainMenuButton(entity));

                    pauseMenuEntities.forEach(p -> p.setPauseBehavior(true));
                    // GameLoop.pause();
                }

                @Override
                public void hudRender() {
                    text.render();
                }

                @Override
                public void destroy() {
                    pauseMenuEntities.forEach(GameLoop::safeDestroy);
                    GameLoop.defer(() -> GameLoop.unpause());
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
            // GameLoop.safeDestroy(main);
            GameLoop.defer(() -> {
                // GameLoop.unpause();
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
