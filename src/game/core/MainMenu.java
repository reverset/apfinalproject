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
import game.ToggleButton;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class MainMenu { // not a fan of this implementation, but I didn't feel like writing a proper implementation for levels/scenes.
    private static final ArrayList<Entity> menuItems = new ArrayList<>();
    private static final ArrayList<Entity> settingsItems = new ArrayList<>();

    public static void clearAndLoad() {
        GameLoop.unpause();
        menuItems.clear();
        settingsItems.clear();
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

        final var settingsEntity = makeSettingsMenu();
        settingsEntity.hide();
        GameLoop.safeTrack(settingsEntity);
        
        GameLoop.setMainCamera(Camera.makeEntity(
			new Transform(), new CameraSettings(Vec2.screenCenter(), 1)
		));

        GameLoop.getMainCameraEntity().register(new ECSystem() {
            private Transform trans;
            @Override
            public void setup() {
                trans = require(Transform.class);
            }

            @Override
            public void frame() {
                trans.position.x = (float) Math.cos(Raylib.GetTime() / 2) * 200;
                trans.position.y = (float) Math.sin(Raylib.GetTime() / 2) * 200;
            }
        });

		GameLoop.track(GameLoop.getMainCameraEntity());
		GameLoop.track(Background.makeEntity());

        
        makeButton("Play", 0, () -> {
            GameLoop.clearAllEntities();
            GameLoop.defer(() -> {
                Game.loadLevel();
            });
        });

        makeButton("Credits", 100, () -> {
            menuItems.forEach(Entity::hide);
            creditsEntity.show();
        });

        makeButton("Settings", 200, () -> {
            menuItems.forEach(Entity::hide);
            settingsEntity.show();
        });

        makeButton("Exit", 300, () -> {
            GameLoop.quit();
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
            .onClick.listen(n -> action.run());
        
        final var e = GameLoop.track(new Entity(text+"::button")
            .addComponent(new Transform(Vec2.screenCenter().add(0, yOffset)))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(button));
        menuItems.add(e);
        return e;
    }

    private static Entity makeButton2(String text, Vec2 pos, Runnable action) {
        final var button = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        button
            .setText(text)
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize()
            .onClick.listen(n -> action.run());
        
        final var e = GameLoop.track(new Entity(text+"::button2")
            .addComponent(new Transform(pos))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(button));

        return e;
    }

    private static Entity makeSettingsToggleButton(String text, float yOffset, boolean defaultState, Runnable onEnable, Runnable onDisable) {
        final var button = new ToggleButton(defaultState, Color.WHITE, 8, 8);
        button
            .setText(text)
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize();
        
        button.onEnabled.listen(n -> onEnable.run());
        button.onDisable.listen(n -> onDisable.run());
        
        final var e = GameLoop.track(new Entity(text+"::settingsToggleButton")
            .addComponent(new Transform(Vec2.screenCenter().add(0, yOffset)))
            .addComponent(new Rect(400, 50, Color.WHITE))
            .register(button));

        settingsItems.add(e);
        return e;
    }

    private static Entity makeTitle() {
        final var title = GameLoop.track(new Entity("title")
            .addComponent(new Transform(Vec2.screenCenter().minus(0, 200)))
            .register(new TextureRenderer("resources/shapesinspacetitle.png", 640, 280).setHudMode(true))
            // .register(new ECSystem() {
            //     private Transform trans;
                
            //     @Override
            //     public void setup() {
            //         trans = require(Transform.class);
            //     }
                
            //     @Override
            //     public void hudRender() {
            //         String text = "Shapes in Space";
            //         int fontSize = 128;
            //         int size = Raylib.MeasureText(text, fontSize);
            //         Raylib.DrawText(text, trans.position.xInt()-size/2, trans.position.yInt(), fontSize, Color.WHITE.getPointerNoUpdate());
            //     }
            // })
        );
        menuItems.add(title);
        return title;
    }

    private static Entity makeCreditsInfo() {
        final var backButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
        backButton
            .setText("Back")
            .setFontSize(34)
            .setOutlineThickness(4)
            .setTextColor(Color.WHITE)
            .centerize();
            
        Entity e = new Entity("credits");
        e
            .addComponent(new Transform(new Vec2(150, 50)))
            .addComponent(new Rect(200, 50, Color.WHITE))
            .register(backButton);

        backButton.onClick.listen((n) -> e.hide());
        
        e.register(new ECSystem() {
            private final Text text = new Text("N/A", new Vec2(), 54, Color.WHITE);

            @Override
            public void setup() {
                text.text = "\n\n\t\tCredits\n\n\t\t" +
                    "Programming \t\t\t\tSebastian\n\t\t" +
                    "Game Design \t\t\t\tSebastian\n\t\t" +
                    "Squiggy Art\t\t\t\t\tLeah\n\t\t" +
                    "Main Menu Art\t\t\tLeah\n\t\t" +
                    "Playtesting\t\t\t\t\tAaron\n\t\t" +
                    "Playtesting\t\t\t\t\tSushant";
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

    private static Entity makeSettingsMenu() {
        Entity entity = new Entity("settings");

        entity.onVisibilityChange.listen(v -> {
            if (v) {
                settingsItems.forEach(Entity::hide);
            } else {
                settingsItems.forEach(Entity::show);
            }
        });

        entity.register(new ECSystem() {
            @Override
            public void setup() {}
            @Override
            public void frame() {
                if (!entity.isHidden()) {
                    if (Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) {
                        entity.hide();
                        menuItems.forEach(Entity::show);
                    }
                }
            }
        });

        Entity backButton = makeButton2("Back", new Vec2(150, 50), () -> {
            entity.hide();
            menuItems.forEach(Entity::show);
        });
        settingsItems.add(backButton);

        makeSettingsToggleButton("Space Dust", -100, Settings.dust, 
            () -> Settings.dust = true, 
            () -> Settings.dust = false);

        makeSettingsToggleButton("Post Processing", 0, GameLoop.isPostProcessEnabled(), 
            () -> GameLoop.enablePostProcessShader(), 
            () -> GameLoop.disablePostProcessShader());
        
        makeSettingsToggleButton("Dynamic Zoom", 100, Settings.dynamicZoom, 
            () -> Settings.dynamicZoom = true, 
            () -> Settings.dynamicZoom = false);

        makeSettingsToggleButton("Camera Shake", 200, Settings.cameraShake, 
            () -> Settings.cameraShake = true, 
            () -> Settings.cameraShake = false);

        return entity;
    }
}
