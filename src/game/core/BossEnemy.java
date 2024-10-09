package game.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    
    public static final float HEXABOMB_COOLDOWN = 1.5f;
    public static final float SPEED = 500;
    public static final float STATE_CHANGE_TIME = 5;

    public static final float CIRCLING_DISTANCE = 200;
    public static final float FAR_CIRCLING_DISTANCE = 2_000;

    public static final int HEAL_AMOUNT = 15;
    public static final float HEALING_THRESHOLD = 0.5f;
    public static final float HEALING_COOLDOWN = 2f;
    public static final float MELEE_COOLDOWN = 1f;
    
    public static final int BASE_DAMAGE = 100;
    
    public static final int PARTS = 15;
    
    private Entity[] parts = new Entity[PARTS];
    
    private State state = State.FAR_CIRCLING;
    
    public Stopwatch healingStopwatch = new Stopwatch();
    private Stopwatch stateChange = new Stopwatch();
    
    private Stopwatch meleeTimer = new Stopwatch();

    private HexaBombLauncher weapon;
    private List<LaserWeapon> skyLasers = new ArrayList<>();

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("The Hexagon Worm", Enemy.class);

        Effect effect = new Effect().setLevel(level);
        effect.addDamageRecievingResponse(d -> {
            return !d.hasExtra(BossBody.class) ? d.damage()*2 : d.damage();
        });

        Supplier<Float> timeSupplier = () -> time()*10;
        entity
            .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Poly(6, RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(Rect.around(RADIUS*2, Color.WHITE))
            .addComponent(effect)
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH, effect).withInvincibilityDuration(0.1f))
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
        effect = require(Effect.class);

        Entity last = entity;
        for (int i = 1; i <= 14; i++) {
            final int j = i;
            Transform t = last.getComponent(Transform.class).orElseThrow();
            EntityOf<BossBody> body = BossBody.makeEntity(this, () -> t.position, () -> tangible.velocity.normalize().multiplyEq(j*4));
            GameLoop.safeTrack(body);
            last = body;
            parts[i-1] = body;
        }

        for (int i = 0; i < 20; i++) {
            LaserWeapon laser = new LaserWeapon(BASE_DAMAGE, trans.position, Vec2.down(), Color.RED, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 5, Optional.of(effect));
            skyLasers.add(laser);
        }

        weapon = new HexaBombLauncher(BASE_HEXABOMB_DAMAGE, BULLET_SPEED, Color.YELLOW, GameTags.ENEMY_TEAM_TAGS, HEXABOMB_COOLDOWN, Optional.empty());
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
                GameLoop.safeTrack(HealingOrb.makeEntity(trans.position.add(Vec2.randomUnit().multiplyEq(50)), BASE_DEATH_HEALING));
            }
        }, entity);

        tangible.onCollision.listen(other -> {
            
            if (other.entity.hasAnyTag(GameTags.PLAYER_TEAM_TAGS)) {
                if (!meleeTimer.hasElapsedSecondsAdvance(MELEE_COOLDOWN)) return;

                Health otherHealth = other.entity.getComponent(Health.class).orElseThrow(); // should probably use more concrete checks
                Transform otherTrans = other.entity.getComponent(Transform.class).orElseThrow();
                
                int dmg = (int) (otherHealth.getMaxHealth()*0.1f);
                otherHealth.damage(new DamageInfo(dmg, null, trans.position.clone(), DamageColor.MELEE));
                
                Vec2 knockback = trans.position.directionTo(otherTrans.position).multiplyEq(1_000);
                other.impulse(knockback);

                // GameLoop.safeTrack(DamageNumber.makeEntity(trans.position.clone(), dmg, Color.ORANGE));
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
        
        trans.rotation = (float) Math.toDegrees(tangible.velocity.getAngle());
    }

    @Override
    public void infrequentUpdate() {
        if (playerTransform == null) return;

        if (stateChange.hasElapsedSecondsAdvance(STATE_CHANGE_TIME)) {
            state = MoreMath.pickRandomEnumeration(State.class);
        }

        if (state == State.FAR_CIRCLING) {
            if (skyLasers.get(0).canFire()) {
                Vec2 pos = playerTransform.position.clone();
                pos.x += Player.SIZE*0.5f;

                for (int i = 0; i < skyLasers.size(); i++) {
                    LaserWeapon laser = skyLasers.get(i);
                    int j = i;
                    laser.chargeUp(() -> pos.minus(j*100-1_000, 1_000), Vec2::down, entity, b -> {});
                }
            }
        }

        if (weapon.canFire()) weapon.fire(trans.position.clone(), trans.position.directionTo(playerTransform.position), entity);

        if (health.getHealthPercentage() < HEALING_THRESHOLD && healingStopwatch.hasElapsedSecondsAdvance(HEALING_COOLDOWN)) {
            health.heal(HEAL_AMOUNT);
        }
    }
    
    @Override
    public void render() {
        for (LaserWeapon laser : skyLasers) {
            laser.render();
        }
    }
}
