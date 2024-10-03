package game.core;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.Triangle;
import game.core.rendering.TriangleRenderer;
import game.ecs.comps.Transform;

public class TriangleEnemy extends Enemy {
    public static final float SIZE = 40;
    public static final float SHOOT_DISTANCE = 2_000;

    public static final int BASE_HEALTH = 60;
    public static final int BASE_DAMAGE = 50;

    Triangle triangle;

    Vec2 desiredPosition;
    Tween<?> movementTween;

    RayWeapon weapon = new RayWeapon(Vec2.zero(), Vec2.up(), BASE_DAMAGE, SHOOT_DISTANCE, 0.1f, 0, new Object[]{GameTags.ENEMY_TEAM})
        .setForce(1_000);

    Color rayColor = new Color(255, 140, 0, 0);
    boolean freezeRotation = false;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        // Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Triangle", Enemy.class);
        entity
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH))
            .addComponent(new Triangle(position, SIZE, SIZE, Color.ORANGE))
            .addComponent(new Rect((int) SIZE, (int) SIZE, Color.WHITE))
            .addComponent(new Effect().setLevel(level))
            // .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new TriangleRenderer())
            .register(new Physics(0, 0, new Vec2(-SIZE*0.5f, -SIZE*0.5f)))
            .register(new HealthBar(
                new Vec2(-SIZE*1.5f, -40), entity.name
            ))
            .register(new TriangleEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }
    
    public Vec2 getFacing() {
        return Vec2.fromAngle((float) Math.toRadians(-trans.rotation+270));
    }

    @Override
    public void setup() {
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> playerTransform = p.getComponent(Transform.class).orElseThrow());

        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);
        effect = require(Effect.class);

        int level = effect.getLevel();

        weapon.setDamage(BASE_DAMAGE + (level-1)*10);
        health.setMaxHealthAndHealth(BASE_HEALTH + (level-1)*10);
        
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
        }, entity);
    }

    @Override
    public void frame() {
        tangible.velocity.setEq(0, 0);
        if (movementStopwatch.hasElapsedSecondsAdvance(5)) {
            desiredPosition = Vec2.screenCenter().screenToWorldEq().addEq(Vec2.randomUnit().multiplyEq(200));
        }

        if (desiredPosition != null && (movementTween == null || !movementTween.isRunning())) {
            movementTween = GameLoop.makeTween(Tween.lerp(trans.position.clone(), desiredPosition), 0.2, v -> {
                trans.position = v;
            });
            movementTween.start();
            desiredPosition = null;


            movementTween.onFinish.listen((n) -> { // Laser animation, should just use TweenAnimation lol
                GameLoop.makeTween(Tween.lerp(0, 50), 3, val -> {
                    weapon.ray.position = trans.position;
                    weapon.ray.direction = getFacing();
                    weapon.ray.updateRay();

                    rayColor.a = val.byteValue();

                    if (rayColor.a > 40) freezeRotation = true;
                }).start().onFinish.listen(nn -> {
                    weapon.fire(trans.position, getFacing());

                    GameLoop.makeTween(Tween.lerp(255, 0), 0.5, val -> {
                        weapon.ray.position = trans.position;
                        weapon.ray.direction = getFacing();
                        weapon.ray.updateRay();
    
                        rayColor.a = val.byteValue();

                        if (rayColor.a == 0) freezeRotation = false;
                    }).start();

                }, entity);
            }, entity);
        }
    }

    @Override
    public void infrequentUpdate() {
        if (freezeRotation) return;
        if (playerTransform == null) return;

        Vec2 pos = playerTransform.position.add(SIZE*0.5f, SIZE*0.5f);
        Vec2 dir = trans.position.directionTo(pos);
        trans.rotation = (float) -Math.toDegrees(dir.getAngle()) - 90; // why does raylib use degrees :(
    }

    @Override
    public void render() {
        weapon.ray.renderEx(15, rayColor);
    }
}
