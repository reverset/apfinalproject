package game.core;

import java.time.Duration;

import game.Color;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class WeaponFactory {
    public static Weapon standardWeapon(Color color, Entity owner, Object[] ignoreTags) {
        return new Weapon(() -> {
            return BulletFactory.standardBullet(5, new Transform(), new Vec2(), color, owner, ignoreTags, BulletFactory.STANDARD_BULLET_LIFE);
        }, 1, BulletFactory.STANDARD_BULLET_SPEED);
    }

    public static RadiusWeapon radiusWeapon(Color color, Entity owner, Object[] ignoreTags) {
        return new RadiusWeapon(() -> {
            return BulletFactory.standardBullet(5, new Transform(), new Vec2(), color, owner, ignoreTags, Duration.ofSeconds(1));
        }, 3, 600f, 45);
    }
}
