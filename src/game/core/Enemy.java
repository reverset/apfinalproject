package game.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import game.Color;
import game.DespawnDistance;
import game.EntityOf;
import game.GameLoop;
import game.Shader;
import game.ShaderUpdater;
import game.Stopwatch;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.core.rendering.ViewCuller;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Enemy extends ECSystem {
    public static final float SPEED = 200;
    public static final int SIZE = 50;
    public static final float DESPAWN_DISTANCE = 5_000;

    public static final int BASE_DAMAGE = 5;
    public static final int BASE_HEALTH = 20;

    public Health health;
    public Rect rect;
    public Transform trans;
    public Tangible tangible;

    Stopwatch movementStopwatch = new Stopwatch();
    
    Optional<Entity> player;
    Transform playerTransform;
    Effect effect;

    private Vec2 desiredDirection = null;

    private Weapon weapon = WeaponFactory.standardWeapon(Color.RED, entity, new Object[]{GameTags.ENEMY_TEAM})
        .setCooldown(3);

    double timeOffset = 0;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        Rect rect = new Rect(SIZE, SIZE, Color.RED);
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Square", Enemy.class);
        entity
            .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Transform(position))
            .addComponent(rect)
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH))
            .addComponent(new Effect().setLevel(level))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new RectRender())
            .register(new Physics(0, 0))
            .register(new HealthBar(
                new Vec2(-rect.width*0.5f, -20), entity.name
            ))
            .register(new Enemy())
            .register(new ViewCuller(Vec2.screen().x+SIZE))
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }


    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        rect = require(Rect.class);
        health = require(Health.class);
        effect = require(Effect.class);

        int level = effect.getLevel();
        weapon.setDamage(level*BASE_DAMAGE);
        health.setMaxHealthAndHealth(BASE_HEALTH+((level-1)*5));
        
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent((p) -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
            GameLoop.defer(() -> {
                entity.register(new DespawnDistance(playerTransform, DESPAWN_DISTANCE));
            });
        });
        
        timeOffset = (Math.random()+0.5) * 2;
        movementStopwatch.start();
    }
    
    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(DestroyEffect.makeEntity(rect.dimensions(), trans.position.clone()));
            GameLoop.safeTrack(HealingOrb.makeEntity(trans.position, 10*effect.getLevel()));
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;

        if (movementStopwatch.hasElapsedSecondsAdvance(timeOffset)) {
            desiredDirection = trans.position.directionTo(playerTransform.position).multiplyEq(SPEED);
        }

        if (weapon.canFire() && BulletFactory.bullets.size() < 60) weapon.fire(rect.getCenter(trans.position), trans.position.directionTo(playerTransform.position));

        if (desiredDirection == null) return;

        
        tangible.velocity.moveTowardsEq(desiredDirection, 100f*delta());

    }
    
}
