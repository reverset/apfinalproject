package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.comps.Transform;

public class SeekingSquare extends Enemy {
    private Optional<Player> playerComp = Optional.empty();

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("Seeking Square", Enemy.class);

        final int width = 50;
        final int height = 50;

        entity
            .addComponent(new Rect(width, height, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Health(10))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .register(new Physics(0, 0, new Vec2(-width/2, -height/2)))
            .register(new RectRender().centerize())
            .register(new HealthBar(new Vec2(-width, -height), entity.name))
            .register(new SeekingSquare())
            .addTags(GameTags.ENEMY_TEAM_TAGS);


        return entity;
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void ready() {
        playerComp = player.flatMap(p -> p.getSystem(Player.class));
        health.onDeath.listen(e -> {
            GameLoop.safeDestroy(entity);
        }, entity);

        tangible.onCollision.listen(other -> {
            if (other.entity == player.orElse(null)) {
                other.entity
                    .getComponent(Health.class)
                    .ifPresent(health -> {
                        health.damage(new DamageInfo(50, entity, null));
                });
                GameLoop.safeDestroy(entity);
            }
        });
    }

    @Override
    public void infrequentUpdate() {
        if (playerTransform == null || player.isEmpty()) return;

        tangible.velocity.moveTowardsEq(trans.position.directionTo(playerComp.get().getCenter()).multiplyEq(1000), 1000*infreqDelta());
    }

    @Override
    public void frame() {
        if (playerTransform == null || player.isEmpty()) return;

        float angle = trans.position.directionTo(playerTransform.position).getAngleDegrees();
        trans.rotation = angle;
    }
}
