package game.core;

import game.Color;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class WeaponFactory {
    public static Weapon standardWeapon(Color color, Entity owner, Object[] ignoreTags) {
        return new Weapon(() -> {
            return BulletFactory.standardBullet(new Transform(), new Vec2(), color, owner, ignoreTags);
        }, 1, BulletFactory.STANDARD_BULLET_SPEED);
    }
}
