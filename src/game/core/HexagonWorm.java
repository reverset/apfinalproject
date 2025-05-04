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

public class HexagonWorm extends Unit {
    public enum State {
        CIRCLING,
        FAR_CIRCLING,
    }

    public static final float RADIUS = 50;
    public static final int SIDES = 6;

    public static final float BULLET_SPEED = 600;

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
    public static final int XP_AWARD = 100;
    
    public static final int BASE_DAMAGE = 100;
    
    public static final int PARTS = 15;
    
    private Entity[] parts = new Entity[PARTS];
    
    private State state = State.FAR_CIRCLING;
    
    public Stopwatch healingStopwatch = Stopwatch.ofGameTime();
    private Stopwatch stateChange = Stopwatch.ofGameTime();
    
    private Stopwatch meleeTimer = Stopwatch.ofGameTime();

    private HexaBombLauncher weapon;
    private List<LaserWeapon> skyLasers = new ArrayList<>();

    public static EntityOf<Unit> makeEntity(Vec2 position, int level) {
        EntityOf<Unit> entity = new EntityOf<>("The Hexagon Worm", Unit.class);

        Effect effect = new Effect().setLevel(level);
        effect.addDamageRecievingResponseExtra(d -> {
            boolean headshot = !d.hasExtra(HexagonTail.class);
            return d.setDamageAndColor(headshot ? d.damage()*2 : d.damage(), headshot ? DamageColor.CRITICAL : DamageColor.NORMAL);
        });

        effect.addDamageScaling(info -> info.damage() * Math.max(1, effect.getLevel()/4));

        Supplier<Float> timeSupplier = () -> time()*10;
        entity
            .addComponent(Shader.fromCacheOrLoad("resources/enemy.frag"))
            .addComponent(new Poly(6, RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Rect((int) (RADIUS * MoreMath.ROOT_TWO), (int) (RADIUS * MoreMath.ROOT_TWO), Color.WHITE))
            .addComponent(effect)
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH * (Math.ceil(effect.getLevel() / 2.0)), effect).withInvincibilityDuration(0.1f))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new HealthBar(new Vec2(-RADIUS, -100), entity.name, true))
            .register(new Physics(0, 0, new Vec2(-RADIUS/MoreMath.ROOT_TWO, -RADIUS/MoreMath.ROOT_TWO)))
            .register(new PolyRenderer())
            .register(new HexagonWorm())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        Entity last = entity;
        for (int i = 1; i <= PARTS; i++) {
            final int j = i;
            Transform t = last.getComponent(Transform.class).orElseThrow();
            EntityOf<HexagonTail> body = HexagonTail.makeEntity(this, () -> t.position, () -> getTangible().velocity.normalize().multiplyEq(j*4));
            GameLoop.safeTrack(body);
            last = body;
            parts[i-1] = body;
        }

        for (int i = 0; i < 20; i++) {
            LaserWeapon laser = new LaserWeapon(BASE_DAMAGE, getTransform().position, Vec2.down(), Color.RED, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 5, Optional.of(getEffect()));
            skyLasers.add(laser);
        }

        weapon = new HexaBombLauncher(BASE_HEXABOMB_DAMAGE, BULLET_SPEED, Color.YELLOW, GameTags.ENEMY_TEAM_TAGS, HEXABOMB_COOLDOWN, Optional.of(getEffect()));
    }
    
    @Override
    public void ready() {
        super.ready();

        stateChange.start();
        healingStopwatch.start();

        getHealth().onDeath.listen(n -> {
            getTeam().grantExp(XP_AWARD);

            GameLoop.safeDestroy(entity);

            for (int i = 0; i < parts.length; i++) {
                GameLoop.safeDestroy(parts[i]);
            }
            
            GameLoop.defer(() -> {
                RandomPowerup.showScreen();
            });
        }, entity);

        getTangible().onCollision.listen(other -> {
            
            if (other.entity.hasAnyTag(GameTags.PLAYER_TEAM_TAGS)) {
                
                Optional<Transform> otherTrans = other.entity.getComponent(Transform.class);
                
                if (!meleeTimer.hasElapsedSecondsAdvance(MELEE_COOLDOWN)) return;
                
                other.entity.getComponent(Health.class).ifPresent(otherHealth -> {
                    int dmg = (int) (otherHealth.getMaxHealth()*0.1f);
                    otherHealth.damageOrHeal(new DamageInfo(dmg, other.entity, null, getTransform().position.clone(), DamageColor.MELEE));
                });
                
                
                if (otherTrans.isPresent()) {
                    Vec2 knockback = getTransform().position.directionTo(otherTrans.get().position).multiplyEq(1_000);
                    other.impulse(knockback);
                }
            }
        }, entity);
    }

    @Override
    public void frame() {
        // if (playerTransform == null) return;
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();
        
        float speedCoeff = state == State.CIRCLING ? 1 : 12;
        float timeCoeff = 2;

        Vec2 offset = Vec2.fromAngle(time()*timeCoeff).multiplyEq(state == State.CIRCLING ? CIRCLING_DISTANCE : FAR_CIRCLING_DISTANCE);

        Vec2 direction = getTransform().position.directionTo(target.getTransform().position.add(offset)).multiply(SPEED*speedCoeff);
        getTangible().velocity.moveTowardsEq(direction, 1000*delta());
        
        getTransform().rotation = (float) Math.toDegrees(getTangible().velocity.getAngle());
    }

    @Override
    public void infrequentUpdate() {
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();

        if (stateChange.hasElapsedSecondsAdvance(STATE_CHANGE_TIME)) {
            state = MoreMath.pickRandomEnumeration(State.class);
        }

        if (state == State.FAR_CIRCLING) {
            if (skyLasers.get(0).canFire()) {
                Vec2 pos = target.getTransform().position.clone();
                // pos.x += Player.SIZE*0.5f;

                for (int i = 0; i < skyLasers.size(); i++) {
                    LaserWeapon laser = skyLasers.get(i);
                    int j = i;
                    laser.chargeUp(() -> pos.minus(j*100-1_000, 1_000), Vec2::down, entity, b -> {});
                }
            }
        }

        if (weapon.canFire()) weapon.fire(getTransform().position.clone(), getTransform().position.directionTo(target.getTransform().position), entity);

        if (getHealth().getHealthPercentage() < HEALING_THRESHOLD && healingStopwatch.hasElapsedSecondsAdvance(HEALING_COOLDOWN)) {
            // health.heal(HEAL_AMOUNT);
            getHealth().heal(new DamageInfo(-HEAL_AMOUNT, entity, null));
        }
    }
    
    @Override
    public void render() {
        for (LaserWeapon laser : skyLasers) {
            laser.render();
        }
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }
}
