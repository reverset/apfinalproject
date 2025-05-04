package game.core;

import java.time.Duration;
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
import game.core.rendering.Circle;
import game.core.rendering.CircleRenderer;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class CircleEnemy extends Unit {
    public static final float RADIUS = 20;
    public static final float SPEED = 1_000;
    public static final float MOVE_DELAY = 3;

    public static final int BASE_DAMAGE = 15;
    public static final int BASE_DEATH_DAMAGE = 50;
    public static final int BASE_HEALTH = 10;

    public static final float BULLET_SPEED = 600;
    public static final float DEATH_BULLET_SPEED = 100;

    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(1);
    public static final Duration DEATH_BULLET_LIFETIME = Duration.ofSeconds(1);

    public static final float DEGREE_PER_BULLET = 15;
    public static final float DEGREE_PER_DEATH_BULLET = 10;

    public static final float WEAPON_COOLDOWN = 1;

    private Vec2 desiredPosition;
    
    private NovaWeapon weapon;
    private NovaWeapon deathWeapon;

    private Rect rect;

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();

    public static EntityOf<Unit> makeEntity(Vec2 position, int level) {
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Unit> entity = new EntityOf<>("Circle", Unit.class);

        Effect effect = new Effect().setLevel(level);
        effect.addDamageScaling(d -> d.damage() + ((int) Math.ceil((level-1)*2)));

        entity
            .addComponent(Shader.fromCacheOrLoad("resources/circle.frag"))
            .addComponent(new Circle(RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH, effect))
            .addComponent(new Rect((int) RADIUS*2, (int) RADIUS*2, Color.WHITE))
            .addComponent(effect)
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new CircleRenderer())
            .register(new Physics(0, 0, new Vec2(-RADIUS, -RADIUS)))
            .register(new HealthBar(
                new Vec2(-RADIUS*2, -50), entity.name
            ))
            .register(new AutoTeamRegister())
            .register(new CircleEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        rect = require(Rect.class);

        getHealth().setMaxHealthAndHealth(BASE_HEALTH + ((int) Math.ceil((getEffect().getLevel()-1)/9) * 50));

        weapon = new NovaWeapon(BASE_DAMAGE, DEGREE_PER_BULLET, BULLET_SPEED, Color.PINK, GameTags.ENEMY_TEAM_TAGS, WEAPON_COOLDOWN, BULLET_LIFETIME, Optional.of(getEffect()));
        deathWeapon = new NovaWeapon(BASE_DEATH_DAMAGE, DEGREE_PER_DEATH_BULLET, DEATH_BULLET_SPEED, Color.PINK, GameTags.NONE, WEAPON_COOLDOWN, DEATH_BULLET_LIFETIME, Optional.of(getEffect()));
    }

    @Override
    public void ready() {
        getHealth().onDeath.listen(n -> {
            deathWeapon.forceFire(getTransform().position, null, entity);
            GameLoop.safeDestroy(entity);
            Team.getTeamByTagOf(entity).grantExp(10);

        }, entity);
    }

    @Override
    public void frame() {
        // if (playerTransform == null) return;
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Target target = ot.get();

        if (movementStopwatch.hasElapsedSecondsAdvance(MOVE_DELAY)) {
            desiredPosition = target.trans().position.add(Vec2.randomUnit().multiply(50));
        }
    }
    
    @Override
    public void infrequentUpdate() {
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Target target = ot.get();
        
        float dist = getTransform().position.distance(target.trans().position);
        if (dist < 100 && weapon.canFire()) weapon.fire(getTransform().position, null, entity);

        if (desiredPosition == null) return;

        getTangible().velocity.moveTowardsEq(getTransform().position.directionTo(desiredPosition).multiplyEq(5 * dist), SPEED*infreqDelta());
    }
}
