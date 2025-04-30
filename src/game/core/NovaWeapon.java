package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public class NovaWeapon extends Weapon2 {
    

    private int baseDmgPerPellet;
    private float bulletSpeed;

    private float radianPerBullet;
    private Color color;
    private Object[] ignoreTags;
    private Duration lifetime;

    public NovaWeapon(int baseDmgPerPellet, float degreePerBullet, float bulletSpeed, Color color, Object[] ignoreTags, float cooldown, Duration lifetime, Optional<Effect> effect) {
        super(cooldown, effect);
        this.baseDmgPerPellet = baseDmgPerPellet;
        this.color = color;
        this.ignoreTags = ignoreTags;
        this.lifetime = lifetime;
        this.radianPerBullet = (float) Math.toRadians(degreePerBullet);
        this.bulletSpeed = bulletSpeed;
    }

    @Override
    void forceFire(Vec2 position, Vec2 direction, Entity owner) {
        for (float rad = 0; rad < Math.PI*2; rad += radianPerBullet) {
            Vec2 dir = Vec2.fromAngle(rad);

            EntityOf<Bullet> bullet = BulletFactory.bullet(baseDmgPerPellet, effect, position.clone(), dir.multiplyEq(bulletSpeed), color, owner, ignoreTags, lifetime, this);
            GameLoop.safeTrack(bullet);
        }
    }
    
}
