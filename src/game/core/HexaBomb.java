package game.core;

import java.time.Duration;

import game.Color;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.Rect;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class HexaBomb extends Bullet {
    public static final float RADIUS = 15;
    public static final float SPEED = 10;
    public static final float FRICTION_COEFF = 200;

    public static final Duration LIFETIME = Duration.ofSeconds(2);
    public static final Duration PELLET_LIFETIME = Duration.ofSeconds(3);

    Duration lifetime;
    Stopwatch detonationStopwatch = new Stopwatch();

    RadiusWeapon detonation;

    RayWeapon altDetonation;
    RayWeapon altDetonation2;
    RayWeapon altDetonation3;
    RayWeapon altDetonation4;
    
    Color rayColor = new Color(255, 255, 0, 0);

    public HexaBomb(Duration lifetime, Entity owner, int damage, Object[] ignoreTags) {
        super(owner, damage, ignoreTags);
        this.lifetime = lifetime;
    }
    
    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        rect = require(Rect.class);

        Poly poly = require(Poly.class);

        detonation = WeaponFactory.radiusWeapon(poly.color, owner, PELLET_LIFETIME, ignoreTags)
            .setDegreePerBullet(15);

        altDetonation = new RayWeapon(Vec2.zero(), Vec2.up(), damage, 2_000, 0.1f, 0, ignoreTags) // cleanup lol
            .setForce(1_000);
        altDetonation2 = new RayWeapon(Vec2.zero(), Vec2.up(), damage, 2_000, 0.1f, 0, ignoreTags)
            .setForce(1_000);
        altDetonation3 = new RayWeapon(Vec2.zero(), Vec2.up(), damage, 2_000, 0.1f, 0, ignoreTags)
            .setForce(1_000);
        altDetonation4 = new RayWeapon(Vec2.zero(), Vec2.up(), damage, 2_000, 0.1f, 0, ignoreTags)
            .setForce(1_000);
    }

    @Override
    public void ready() {
        detonationStopwatch.start();
    }

    @Override
    public void frame() {
        
    }

    @Override
    public void infrequentUpdate() {
        tangible.velocity.moveTowardsEq(Vec2.ZERO, FRICTION_COEFF*infreqDelta());

        if (detonationStopwatch.hasElapsedAdvance(lifetime) && !altDetonation.isCharging()) {
            
            if (Math.random() >= 0.5) {
                altDetonation.setDamage(getDamage()*2);
                altDetonation.chargeUp(() -> trans.position, Vec2::right, rayColor, entity, b -> {}).onFinish.listen((n) -> {
                    GameLoop.runAfter(entity, Duration.ofMillis(500), () -> GameLoop.safeDestroy(entity));
                }, entity);

                altDetonation2.chargeUp(() -> trans.position, Vec2::up, rayColor, entity, b -> {});
                altDetonation3.chargeUp(() -> trans.position, Vec2::left, rayColor, entity, b -> {});
                altDetonation4.chargeUp(() -> trans.position, Vec2::down, rayColor, entity, b -> {});
            } else {
                GameLoop.safeDestroy(entity);
                detonation.setDamage(getDamage());
                detonation.forceFire(trans.position, null);
            }
        }
    }

    @Override
    public void render() {
        altDetonation.ray.renderEx(15, rayColor);
        altDetonation2.ray.renderEx(15, rayColor);
        altDetonation3.ray.renderEx(15, rayColor);
        altDetonation4.ray.renderEx(15, rayColor);
    }
    
}
