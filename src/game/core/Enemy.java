package game.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Shader;
import game.ShaderUpdater;
import game.Stopwatch;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Enemy extends ECSystem {
    public static final float SPEED = 200;
    public static final int SIZE = 50;

    public static EntityOf<Enemy> makeEntity(Vec2 position) {
        Rect rect = new Rect(SIZE, SIZE, Color.RED);
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Enemy", Enemy.class);
        entity
            .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Transform(position))
            .addComponent(rect)
            .addComponent(new Tangible())
            .addComponent(new Health(20))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new RectRender())
            .register(new Physics())
            .register(new HealthBar(
                new Vec2(-rect.width*0.5f, -20), "Enemy"
            ))
            .register(new Enemy())
            .addTag(GameTags.ENEMY);

        return entity;
    }

    public Health health;
    private Transform trans;
    private Tangible tangible;
    private Rect rect;

    private Stopwatch movementStopwatch = new Stopwatch();
    private Stopwatch shootStopwatch = new Stopwatch();
    
    private Optional<Entity> player;
    private Transform playerTransform;

    private Vec2 desiredDirection = null;

    double timeOffset = 0;

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        rect = require(Rect.class);
        health = require(Health.class);
        
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent((p) -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
        });
        
        timeOffset = (Math.random()+0.5) * 2;
        movementStopwatch.start();
        shootStopwatch.start();
    }
    
    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(DestroyEffect.makeEntity(rect.dimensions(), trans.position.clone()));
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;

        if (movementStopwatch.hasElapsedSecondsAdvance(timeOffset)) {
            desiredDirection = trans.position.directionTo(playerTransform.position).multiplyEq(SPEED);
        }

        if (shootStopwatch.hasElapsedSecondsAdvance(1)) {
            Entity bullet = BulletFactory.standardBullet(
                new Transform(rect.getCenter(trans.position), trans.rotation), trans.position.directionTo(playerTransform.position), Color.RED, entity);

            GameLoop.safeTrack(bullet);
        }

        if (desiredDirection == null) return;

        
        tangible.velocity.moveTowardsEq(desiredDirection, 100f*delta());

    }
    
}
