package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Circle;
import game.core.rendering.CircleRenderer;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class HealingOrb extends ECSystem {
    public static final float RADIUS = 10;

    Optional<Player> player = Optional.empty();
    
    Transform trans;
    Tangible tangible;
    
    int life;

    public static EntityOf<HealingOrb> makeEntity(Vec2 position, int life) {
        EntityOf<HealingOrb> entity = new EntityOf<>("Healing Orb", HealingOrb.class);
        
        entity
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Circle(RADIUS, Color.GREEN))
            .addComponent(Rect.around(RADIUS*2, Color.GREEN))
            .register(new CircleRenderer())
            .register(new Physics(2, 0, new Vec2(-RADIUS, -RADIUS)))
            .register(new HealingOrb(life))
            .addTags(GameTags.PLAYER_TEAM);
        
        return entity;
    }

    public HealingOrb(int life) {
        this.life = life;
    }

    @Override
    public void setup() {
        tangible = require(Tangible.class);
        trans = require(Transform.class);
        
        GameLoop.findEntityByTag(GameTags.PLAYER).ifPresent(e -> {
            player = e.getSystem(Player.class);
        });;
    }

    @Override
    public void ready() {
        tangible.onCollision.listen(physics -> {
            if (physics.entity.hasTag(GameTags.PLAYER)) {
                GameLoop.safeDestroy(entity);
                Health health = physics.entity.getComponent(Health.class).orElseThrow();
                health.heal(life);

                GameLoop.safeTrack(DamageNumber.makeEntity(trans.position, life, Color.GREEN));
            }
        }, entity);
    }

    @Override
    public void infrequentUpdate() {
        if (player.isEmpty()) return;

        tangible.velocity.moveTowardsEq(trans.position.directionTo(player.get().getCenter()).multiplyEq(1000), 1000*infreqDelta());
    }
}
