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
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.core.rendering.ViewCuller;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class Square extends Unit {
    public static final float SPEED = 200;
    public static final int SIZE = 50;

    public static final int BASE_DAMAGE = 5;
    public static final int BASE_HEALTH = 20;

    public static final float BULLET_SPEED = 600;
    public static final float BULLET_COOLDOWN = 3;
    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(3);

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();
    private Vec2 desiredDirection = null;

    // private Weapon weapon = WeaponFactory.standardWeapon(Color.RED, entity, new Object[]{GameTags.ENEMY_TEAM})
    //     .setCooldown(3);

    private SimpleWeapon weapon;

    private double timeOffset = 0;

    private Rect rect;

    public static EntityOf<Unit> makeEntity(Vec2 position, int level) {
        Rect rect = new Rect(SIZE, SIZE, Color.RED);
        
        Supplier<Float> timeSupplier = ECSystem::time; // ????
        EntityOf<Unit> entity = new EntityOf<>("Square", Unit.class);

        Effect effect = new Effect()
            .setLevel(level);
            
        effect.addDamageScaling(d -> effect.getLevel()*d.damage() + (int)Math.pow(1.1, level));

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
            .register(new Square())
            .register(new ViewCuller(Vec2.screen().x+SIZE))
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        rect = require(Rect.class);
        
        getHealth().setMaxHealthAndHealth(BASE_HEALTH+((getEffect().getLevel()-1)*5));

        timeOffset = (Math.random()+0.5) * 2;
        movementStopwatch.start();

        weapon = new SimpleWeapon(BASE_DAMAGE, BULLET_SPEED, Color.RED, new Object[]{GameTags.ENEMY_TEAM}, BULLET_LIFETIME, BULLET_COOLDOWN, Optional.of(getEffect()));
    }
    
    @Override
    public void ready() {
        super.ready();
        getHealth().onDeath.listen(n -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(DestroyEffect.makeEntity(rect.dimensions(), getTransform().position.clone()));

            Team.getTeamByTagOf(entity).grantExp(10);
        }, entity);
    }

    @Override
    public void frame() {
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();

        float distance = target.getTransform().position.distance(getTransform().position);
        if (movementStopwatch.hasElapsedSecondsAdvance(timeOffset)) {
            float desiredSpeed = SPEED;
            if (distance > 500) desiredSpeed *= 2;
            desiredDirection = getTransform().position.directionTo(target.getTransform().position).multiplyEq(desiredSpeed);
        }

        if (weapon.canFire() && BulletFactory.bullets.size() < 60) weapon.fire(getTransform().position, getTransform().position.directionTo(target.getTransform().position), entity);

        if (desiredDirection == null) return;

        
        getTangible().velocity.moveTowardsEq(desiredDirection, 1_000f*delta());

    }
}
