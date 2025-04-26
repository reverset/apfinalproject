package game.core;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import game.Color;
import game.DespawnDistance;
import game.EntityOf;
import game.GameLoop;
import game.RecoverableException;
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

    public static final float BULLET_SPEED = 600;
    public static final float BULLET_COOLDOWN = 3;
    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(3);

    public Health health;
    public Rect rect;
    public Transform trans;
    public Tangible tangible;

    Stopwatch movementStopwatch = Stopwatch.ofGameTime();
    
    Optional<Entity> player;
    Transform playerTransform;
    Effect effect;

    private Vec2 desiredDirection = null;

    // private Weapon weapon = WeaponFactory.standardWeapon(Color.RED, entity, new Object[]{GameTags.ENEMY_TEAM})
    //     .setCooldown(3);

    private SimpleWeapon weapon;

    double timeOffset = 0;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        Rect rect = new Rect(SIZE, SIZE, Color.RED);
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Square", Enemy.class);

        Effect effect = new Effect()
            .setLevel(level);
            
        effect.addDamageScaling(d -> effect.getLevel()*d.damage());

        entity
            .addComponent(Shader.fromCacheOrLoad("resources/enemy.frag"))
            .addComponent(new Transform(position))
            .addComponent(rect)
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH, effect))
            .addComponent(effect)
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


    protected void basicSetup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        rect = require(Rect.class);
        health = require(Health.class);
        effect = require(Effect.class);

        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        playerTransform = player
            .flatMap(p -> p.getComponent(Transform.class))
            .orElse(null);
    }

    @Override
    public void setup() {
        basicSetup();

        int level = effect.getLevel();
        health.setMaxHealthAndHealth(BASE_HEALTH+((level-1)*5));
        
        timeOffset = (Math.random()+0.5) * 2;
        movementStopwatch.start();

        weapon = new SimpleWeapon(BASE_DAMAGE, BULLET_SPEED, Color.RED, new Object[]{GameTags.ENEMY_TEAM}, BULLET_LIFETIME, BULLET_COOLDOWN, Optional.of(effect));
    }
    
    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(DestroyEffect.makeEntity(rect.dimensions(), trans.position.clone()));
            GameLoop.safeTrack(HealingOrb.makeEntity(trans.position, 10 + (5 * (effect.getLevel()-1))));
            player
                .flatMap(en -> en.getSystem(Player.class))
                .ifPresent(p -> p.getExpAccumulator().accumulate(10));
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;

        if (movementStopwatch.hasElapsedSecondsAdvance(timeOffset)) {
            desiredDirection = trans.position.directionTo(playerTransform.position).multiplyEq(SPEED);
        }

        if (weapon.canFire() && BulletFactory.bullets.size() < 60) weapon.fire(rect.getCenter(trans.position), trans.position.directionTo(playerTransform.position), entity);

        if (desiredDirection == null) return;

        
        tangible.velocity.moveTowardsEq(desiredDirection, 100f*delta());

    }
    
    public boolean isBossEnemy() {
        return false;
    }
    
}
