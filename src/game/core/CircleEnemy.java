package game.core;

import java.util.List;
import java.util.function.Supplier;

import game.Color;
import game.DespawnDistance;
import game.EntityOf;
import game.GameLoop;
import game.Shader;
import game.ShaderUpdater;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Circle;
import game.core.rendering.CircleRenderer;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class CircleEnemy extends Enemy {
    public static final float RADIUS = 20;
    public static final float SPEED = 1_000;
    public static final float MOVE_DELAY = 3;

    private Vec2 desiredPosition;
    
    public static EntityOf<Enemy> makeEntity(Vec2 position) {
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Circle Enemy", Enemy.class);
        entity
            .addComponent(new Shader("resources/circle.frag"))
            .addComponent(new Circle(RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(10))
            .addComponent(new Rect((int) RADIUS*2, (int) RADIUS*2, Color.WHITE))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new CircleRenderer())
            .register(new Physics(0, 0, new Vec2(-RADIUS, -RADIUS)))
            .register(new HealthBar(
                new Vec2(-RADIUS*2, -50), entity.name
            ))
            .register(new CircleEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }



    private RadiusWeapon weapon = WeaponFactory.radiusWeapon(Color.PINK, entity, new Object[]{GameTags.ENEMY_TEAM})
        .setDegreePerBullet(15)    
        .setDamage(15);

    private RadiusWeapon deathWeapon = WeaponFactory.radiusWeapon(Color.PINK, entity, new Object[]{})
        .setDegreePerBullet(10)
        .setDamage(50)
        .setSpeed(100);

    @Override
    public void setup() {
        health = require(Health.class);
        rect = require(Rect.class);
        trans = require(Transform.class);
        tangible = require(Tangible.class);

        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
            GameLoop.defer(() -> {
                entity.register(new DespawnDistance(playerTransform, DESPAWN_DISTANCE));
            });
        });
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            deathWeapon.forceFire(trans.position, null);
            GameLoop.safeDestroy(entity);
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;

        if (movementStopwatch.hasElapsedSecondsAdvance(MOVE_DELAY)) {
            desiredPosition = playerTransform.position.add(Vec2.randomUnit().multiply(50));
        }
    }
    
    @Override
    public void infrequentUpdate() {
        if (playerTransform == null) return;
        
        float dist = trans.position.distance(playerTransform.position);
        if (dist < 100 && weapon.canFire()) weapon.fire(trans.position, null);

        if (desiredPosition == null) return;

        tangible.velocity.moveTowardsEq(trans.position.directionTo(desiredPosition).multiplyEq(5 * dist), SPEED*infreqDelta());
    }
}
