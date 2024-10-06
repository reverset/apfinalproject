package game.core;

import java.time.Duration;
import java.util.Arrays;

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
            .setDegreePerBullet(15)
            .setDamage(getDamage());
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

        if (detonationStopwatch.hasElapsed(lifetime)) {
            GameLoop.safeDestroy(entity);
            detonation.forceFire(trans.position, null);
        }
    }

    @Override
    public void render() {
        
    }
    
}
