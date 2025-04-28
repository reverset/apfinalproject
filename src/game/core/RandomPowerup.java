package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Button;
import game.Color;
import game.DamageOverTime;
import game.GameLoop;
import game.HealthSyphon;
import game.Text;
import game.Tween;
import game.Vec2;
import game.core.rendering.HealthPowerup;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class RandomPowerup {
    public static void showScreen() {
        Text text = new Text("SELECT A POWERUP", Vec2.screenCenter().addEq(0, -200), 54, Color.WHITE).center();
        GameLoop.track(Text.makeEntity(text).addTags("powerupselect"));

        GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter(), new Diamond(null, null, null, 0)));
        // GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(400, 0), new HealthPowerup(null, null, null, 0)));
        // GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(400, 0), new DamageOverTime(null, null, null, 0)));
        GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(400, 0), new HealthSyphon(null, null, null, 0)));
        GameLoop.track(RandomPowerup.makeButton(Vec2.screenCenter().addEq(-400, 0), new HealthRegenPowerup(null, null, null, 0)));
    }

    public static Entity makeButton(Vec2 pos, Powerup powerup) {
        Entity entity = new Entity("random powerup");
        entity.runWhilePaused = true;
        
        Rect rect = new Rect(200, 200, Color.DARK_RED);
        
        Vec2 desiredPos = rect.centerize(pos);
        Transform trans = new Transform(desiredPos.clone());
        trans.position.y = -100;

        entity
            .addComponent(trans)
            .addComponent(rect)
            .register(new RectRender().setHudMode(true))
            .register(new ECSystem() {

                Text name = new Text(powerup.getName(), trans.position, 24, Color.WHITE);
                Text description = null;

                @Override
                public void setup() {
                    GameLoop.pause();
                    GameLoop.makeTween(Tween.overEase(-200, desiredPos.y, 1), 1, val -> {
                        trans.position.y = val;
                    }).runWhilePaused(true).start().onFinish.listenOnce(n -> {
                        description = new Text(powerup.getDescription(), trans.position.add(0, 200), 24, Color.WHITE);
                    });
                }

                @Override
                public void hudRender() {
                    name.render();
                    if (description != null) description.renderWithNewlines();
                }
                
            })
            .register(new Button(() -> {
                GameLoop.runAfter(null, Duration.ofMillis(50), () -> {
                    GameLoop.unpause();
                });

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

            })).addTags("powerupselect");
        
        return entity;
    }
}
