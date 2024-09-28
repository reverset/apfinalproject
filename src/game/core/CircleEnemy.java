package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Circle;
import game.core.rendering.CircleRenderer;
import game.core.rendering.Rect;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class CircleEnemy extends Enemy {
    public static final float RADIUS = 20;
    public static final float SPEED = 1_000;
    public static final float MOVE_DELAY = 5;

    public static EntityOf<Enemy> makeEntity(Vec2 position) {
        
        // Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("CircleEnemy", Enemy.class);
        entity
            // .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Circle(RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(10))
            .addComponent(new Rect((int) RADIUS*2, (int) RADIUS*2, Color.WHITE))
            // .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new CircleRenderer())
            .register(new Physics(0, 1, new Vec2(-RADIUS, -RADIUS)))
            .register(new HealthBar(
                new Vec2(-RADIUS*2, -50), "Circle Enemy"
            ))
            .register(new CircleEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }


    private Optional<Entity> player;
    private Transform playerTransform;

    private Stopwatch movementStopwatch = new Stopwatch();
    private Vec2 desiredPosition;

    private Weapon weapon = WeaponFactory.radiusWeapon(Color.RED, entity, new Object[]{GameTags.ENEMY_TEAM});

    @Override
    public void setup() {
        health = require(Health.class);
        rect = require(Rect.class);
        trans = require(Transform.class);
        tangible = require(Tangible.class);

        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
        });
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;

        if (movementStopwatch.hasElapsedSecondsAdvance(MOVE_DELAY)) {
            desiredPosition = playerTransform.position.add(Vec2.randomUnit().multiply(300));
        }

        if (weapon.canFire()) weapon.fire(trans.position, null);

        
    }
    
    @Override
    public void infrequentUpdate() {
        if (desiredPosition == null) return;
        tangible.velocity.moveTowardsEq(trans.position.directionTo(desiredPosition).multiplyEq(400), SPEED*infreqDelta());
    }
}
