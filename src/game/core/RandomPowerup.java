package game.core;

import java.util.Optional;

import game.Button;
import game.Color;
import game.GameLoop;
import game.Text;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class RandomPowerup {
    public static Entity makeButton(Vec2 pos, Powerup powerup) {
        Entity entity = new Entity("random powerup");
        entity.runWhilePaused = true;
        
        Rect rect = new Rect(200, 200, Color.DARK_RED);
        Transform trans = new Transform(rect.centerize(pos));
        entity
            .addComponent(trans)
            .addComponent(rect)
            .register(new RectRender().setHudMode(true))
            .register(new ECSystem() {

                Text name = new Text(powerup.getName(), trans.position.clone(), 24, Color.WHITE);
                Text description = new Text(powerup.getDescription(), trans.position.add(0, 200), 24, Color.WHITE);

                @Override
                public void setup() {
                    GameLoop.pause();
                }

                @Override
                public void hudRender() {
                    name.render();
                    description.render();
                }
                
            })
            .register(new Button(() -> {
                GameLoop.defer(() -> {
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
                    }
                    
                });

                GameLoop.safeDestroy(entity);
            }));
        
        return entity;
    }
}
