package game.core;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.MoreMath;
import game.Shader;
import game.ShaderUpdater;
import game.Stopwatch;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
import game.core.rendering.Rect;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class BossEnemy extends Enemy {
    public enum State {
        CIRCLING,
        FAR_CIRCLING,
    }

    public static final float RADIUS = 50;
    public static final int SIDES = 6;

    public static final int BASE_HEALTH = 500;
    public static final int BASE_HEXABOMB_DAMAGE = 35;
    public static final int BASE_DEATH_HEALING = 35;

    public static final float SPEED = 500;
    public static final float STATE_CHANGE_TIME = 5;

    public static final float CIRCLING_DISTANCE = 200;
    public static final float FAR_CIRCLING_DISTANCE = 2_000;

    public static final int PARTS = 15;

    private Entity[] parts = new Entity[PARTS];

    private State state = State.FAR_CIRCLING;

    private Stopwatch stateChange = new Stopwatch();

    private Weapon weapon = WeaponFactory.hexaBombWeapon(Color.YELLOW, entity, new Object[]{GameTags.ENEMY_TEAM})
        .setDamage(BASE_DAMAGE)
        .setCooldown(1);

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("Boss", Enemy.class);

        Supplier<Float> timeSupplier = () -> time()*10; // ????
        entity
            .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Poly(6, RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(Rect.around(RADIUS*2, Color.WHITE))
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH).withInvincibilityDuration(0.1f))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new HealthBar(new Vec2(-RADIUS, -100), entity.name, true))
            .register(new Physics(0, 0, new Vec2(-RADIUS, -RADIUS)))
            .register(new PolyRenderer())
            .register(new BossEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);

        Entity last = entity;
        for (int i = 1; i < 15; i++) {
            final int j = i;
            Transform t = last.getComponent(Transform.class).orElseThrow();
            EntityOf<BossBody> body = BossBody.makeEntity(this, () -> t.position, () -> tangible.velocity.normalize().multiplyEq(j*4));
            GameLoop.safeTrack(body);
            last = body;
            parts[i-1] = body;
        }
    }

    @Override
    public void ready() {
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
        });

        health.onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);

            for (int i = 0; i < parts.length; i++) {
                GameLoop.safeDestroy(parts[i]);
                GameLoop.safeTrack(HealingOrb.makeEntity(trans.position, BASE_DEATH_HEALING));
            }
        }, entity);
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;
        
        float speedCoeff = state == State.CIRCLING ? 1 : 12;
        float timeCoeff = 2;

        Vec2 offset = Vec2.fromAngle(time()*timeCoeff).multiplyEq(state == State.CIRCLING ? CIRCLING_DISTANCE : FAR_CIRCLING_DISTANCE);

        Vec2 direction = trans.position.directionTo(playerTransform.position.add(offset)).multiply(SPEED*speedCoeff);
        tangible.velocity.moveTowardsEq(direction, 1000*delta());
    }

    @Override
    public void infrequentUpdate() {
        if (playerTransform == null) return;

        if (stateChange.hasElapsedSecondsAdvance(STATE_CHANGE_TIME)) {
            state = MoreMath.pickRandomEnumeration(State.class);
            System.out.println("BOSS SWITCHED TO " + state);
        }

        
        if (weapon.canFire()) weapon.fire(trans.position, trans.position.directionTo(playerTransform.position));
    }
    
}
