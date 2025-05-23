package game.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.raylib.Raylib;

import game.BetterButton;
import game.Button;
import game.Color;
import game.GameLoop;
import game.RecoverableException;
import game.Shader;
import game.Signal;
import game.Text;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

// this class is ugly. I'm sorry, its 1:34 am.
public class RandomPowerup {
    private static final Signal<Void> rerollSignal = new Signal<>();
    
    private static final List<Supplier<Powerup>> powerups = List.of(
        () -> new Diamond(null, null, null, 0),
        () -> new HealthSyphon(null, null, null, 0),
        () -> new HealthRegenPowerup(null, null, null, 0),
        () -> new DecayPowerup(null, null, null, 0),
        () -> new SquiggyPowerup(null, null, null, 0),
        () -> new Absorption(null, null, null, 0),
        () -> new BlahajPowerup(null, null, null, 0),
        () -> new RhombusPowerup(null, null, null, 0),
        () -> new FluffyPowerup(null, null, null, 0),
        () -> new DashPowerup(null, null, null, 0)
    );

    private static ArrayList<Powerup> getSelectables(Effect playerC) {
        ArrayList<Powerup> select = new ArrayList<>();
        for (final var pow : powerups) {
            final var playerPow = playerC.getPowerUp(pow.get().getClass());
            
            // skip powerups already at max level;
            if (playerPow.isPresent() && !playerPow.get().canLevelUp()) continue;
            
            select.add(pow.get());
        }
        return select;
    }

    public static void showScreen() {
        final var player = GameLoop.findEntityByTag(GameTags.PLAYER);
        if (player.isEmpty()) return; // player died do not show screen.
        final var playerC = player.get().getComponent(Effect.class).orElseThrow(() -> new RecoverableException("Player is missing Effect."));
            
        ArrayList<Powerup> select = getSelectables(playerC);
        
        if (select.size() == 0) return; // All powerups at max level;

        if (playerC.getPowerups().size() == 0) {
            GameLoop.defer(() -> {
                final var button = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
                button
                    .setText("Reroll")
                    .setFontSize(34)
                    .setOutlineThickness(4)
                    .setTextColor(Color.WHITE)
                    .centerize()
                    .onClick.listen(n -> rerollSignal.emit(null));
                
                final var e = GameLoop.track(new Entity("rerollbutton")
                    .addComponent(new Transform(Vec2.screenCenter().add(0, 300)))
                    .addComponent(new Rect(200, 50, Color.WHITE))
                    .register(button))
                    .addTags("powerupselect")
                    .setPauseBehavior(true);
                
                rerollSignal.listen(n -> GameLoop.defer(() -> spawnPowerups(getSelectables(playerC))), e);

                final var t = e.getComponent(Transform.class).orElseThrow(() -> new RecoverableException("impossible"));

                GameLoop.makeTween(Tween.overEase(0, 100, 1), 1, val -> {
                    t.position.y = GameLoop.SCREEN_HEIGHT - val;
                }).start().runWhilePaused(true);
            });
        }

        GameLoop.pause();

        GameLoop.track(makeBackground());
        Text text = new Text("SELECT A POWERUP", Vec2.screenCenter().addEq(0, -200), 54, Color.WHITE).center();
        GameLoop.track(Text.makeEntity(text).addTags("powerupselect"));
        
        spawnPowerups(select);
    }

    private static void spawnPowerups(List<Powerup> select) {
        GameLoop.destroyAll(e -> e.name.equals("random powerup"));

        GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter(), select.remove((int) (Math.random()*select.size()))));
        if (select.size() > 0) GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(400, 0), select.remove((int) (Math.random()*select.size()))));
        if (select.size() > 0) GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(-400, 0), select.remove((int) (Math.random()*select.size()))));
    }

    public static Entity makeBackground() {
        Entity e = new Entity("powerupbackground");
        e.addTags("powerupselect");
        e.setPauseBehavior(true);

        e.register(new ECSystem() {
            private int redX = -GameLoop.SCREEN_WIDTH;
            private int blackX = GameLoop.SCREEN_WIDTH;
            private int blackHeight = 500;
            private Shader shader = Shader.fromCacheOrLoad("resources/powerupbackground.frag");

            private Color blackColor = Color.BLACK.cloneIfImmutable().setAlpha(230);       

            @Override
            public void setup() {
                GameLoop.makeTween(Tween.lerp(-GameLoop.SCREEN_WIDTH, 0), 0.5, val -> {
                    redX = val.intValue();
                    blackX = -val.intValue();
                }).start().entity.setPauseBehavior(true);
            }

            @Override
            public void frame() {
                shader.setShaderValue("time", (float)Raylib.GetTime()/2);
            }

            @Override
            public void hudRender() {
                shader.with(() -> {
                    Raylib.DrawRectangle(redX, 0, GameLoop.SCREEN_WIDTH, GameLoop.SCREEN_HEIGHT, Color.RED.getPointer());
                });
                Raylib.DrawRectangle(blackX, GameLoop.SCREEN_HEIGHT/2 - blackHeight/2, GameLoop.SCREEN_WIDTH, blackHeight, blackColor.getPointer());
            }
        });

        return e;
    }

    public static Entity makeButton(Vec2 pos, Powerup powerup) {
        Entity entity = new Entity("random powerup");
        entity.runWhilePaused = true;
        
        Rect rect = new Rect(200, 200, Color.DARK_RED);
        Vec2 p = rect.centerize(pos);
        
        TextureRenderer tex = new TextureRenderer(powerup.getIconPath(), 200, 200).setHudMode(true);

        Transform trans = new Transform(pos.clone());
        trans.position.y = -100;
        entity
            .addComponent(trans)
            .addComponent(rect)
            .register(tex)
            // .register(new RectRender().setHudMode(true))
            .register(new ECSystem() {

                Text name = new Text(powerup.getName(), trans.position.clone(), 24, Color.WHITE);
                Text description = null;

                @Override
                public void setup() {
                    GameLoop.makeTween(Tween.overEase(-200, pos.y, 1), 1, val -> {
                        trans.position.y = val;
                    }).runWhilePaused(true).start().onFinish.listenOnce(n -> {
                        name.position.setEq(trans.position.x - 100, trans.position.y - 100);
                        description = new Text(powerup.getDescription(), trans.position.add(-100, 100), 24, Color.WHITE);
                    });
                }

                @Override
                public void hudRender() {
                    name.render();
                    if (description != null) description.renderWithNewlines();
                }

                public void destroy() {
                    // tex.getTexture().
                }
                
            })
            .register(new Button(() -> {
                // GameLoop.runAfter(null, Duration.ofMillis(50), () -> {
                //     GameLoop.unpause();
                // });
                GameLoop.defer(() -> GameLoop.unpause());

                Optional<Entity> playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);

                playerEntity.ifPresent(player -> { // this is atrocious, please improve eventually FIXME
                    Effect effect = player.getComponent(Effect.class).orElseThrow();
                    if (!effect.hasPowerUpThenIncrementLevel(powerup.getClass())) {
                        powerup.entity = player;
                        powerup.effect = effect;
                        powerup.level = 1;
                        player.register(powerup);
                        powerup.ready();
                    }
                    
                });

                for (var selects : GameLoop.findEntitiesByTag("powerupselect")) {
                    GameLoop.safeDestroy(selects);
                }

            }, true)).addTags("powerupselect");
        
        return entity;
    }
}
