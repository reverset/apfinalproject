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

    Triangle triangle;

    Vec2 desiredPosition;
    Tween<?> movementTween;

    public static EntityOf<Enemy> makeEntity(Vec2 position) {
        // Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Triangle", Enemy.class);
        entity
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(60))
            .addComponent(new Triangle(position, SIZE, SIZE, Color.RED))
            .addComponent(new Rect((int) SIZE, (int) SIZE, Color.WHITE))
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

    @Override
    public void setup() {
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> playerTransform = p.getComponent(Transform.class).orElseThrow());

        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);
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

        if (desiredPosition != null && (movementTween == null || movementTween.isFinished())) {
            movementTween = GameLoop.makeTween(Tween.lerp(trans.position.clone(), desiredPosition), 0.2, v -> {
                trans.position = v;
            });
            movementTween.start();
        }
    }
    
    @Override
    public void infrequentUpdate() {
        Vec2 pos = playerTransform.position.add(SIZE*0.5f, SIZE*0.5f);
        Vec2 dir = trans.position.directionTo(pos);
        trans.rotation = (float) -Math.toDegrees(dir.getAngle()) - 90;
    }

    @Override
    public void render() {
        
    }
}
