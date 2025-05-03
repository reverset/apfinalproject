package game.core;

import java.util.Optional;

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

    LaserWeapon weapon;

    Color rayColor = new Color(255, 140, 0, 0);
    boolean freezeRotation = false;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        // Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Enemy> entity = new EntityOf<>("Triangle", Enemy.class);

        Effect effect = new Effect().setLevel(level);
        effect.addDamageScaling(d -> d.damage() + (effect.getLevel()-1)*10);

        entity
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH, effect))
            .addComponent(new Triangle(position, SIZE, SIZE, Color.ORANGE))
            .addComponent(new Rect((int) SIZE, (int) SIZE, Color.WHITE))
            .addComponent(effect)
            // .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new TriangleRenderer())
            .register(new Physics(0, 0, new Vec2(-SIZE*0.5f, -SIZE*0.5f)))
            .register(new HealthBar(
                new Vec2(-SIZE*1.5f, -40), entity.name
            ))
            .register(new AutoTeamRegister())
            .register(new TriangleEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }
    
    public Vec2 getFacing() {
        return Vec2.fromAngle((float) Math.toRadians(-trans.rotation+270));
    }

    @Override
    public void setup() {
        // player = GameLoop.findEntityByTag(GameTags.PLAYER);
        // player.ifPresent(p -> {
        //     playerTransform = p.getComponent(Transform.class).orElseThrow();
        //     playerTangible = p.getComponent(Tangible.class).orElseThrow();
        // });

        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);
        effect = require(Effect.class);

        health.setMaxHealthAndHealth(BASE_HEALTH + (effect.getLevel()-1)*10);
        
        weapon = new LaserWeapon(BASE_DAMAGE, trans.position, getFacing(), Color.ORANGE, SHOOT_DISTANCE, 1_000, 15, 0, new Object[]{GameTags.ENEMY_TEAM}, 0.1f, Optional.of(effect));
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
            // player
            //     .flatMap(en -> en.getSystem(Player.class))
            //     .ifPresent(p -> p.getExpAccumulator().accumulate(10));
            Team.getTeamByTagOf(entity).grantExp(10);

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

            movementTween.onFinish.listen(n -> {
                weapon.chargeUp(() -> trans.position, this::getFacing, entity, impending -> freezeRotation = impending);
            }, entity);
        }
    }

    @Override
    public void infrequentUpdate() {
        if (freezeRotation) return;
        // if (playerTransform == null || playerTangible == null) return;
        Team team = Team.getTeamByTagOf(entity);
        final var ot = team.findTarget(trans.position);
        if (ot.isEmpty()) return;
        Target target = ot.get();
        if (target.physics().isEmpty()) return;

        Vec2 pos = target.trans().position.add(target.physics().get().getTangible().velocity.divide(2));
        Vec2 dir = trans.position.directionTo(pos);
        trans.rotation = (float) -Math.toDegrees(dir.getAngle()) - 90; // why does raylib use degrees :(
    }

    @Override
    public void render() {
        weapon.render();
    }
}
