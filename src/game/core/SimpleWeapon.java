package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public class SimpleWeapon extends Weapon2 {

    private int baseDamage;
    private Color color;
    private Duration lifetime;
    private float bulletSpeed;

    public SimpleWeapon(int baseDamage, float bulletSpeed, Color color, Object[] ignoreTags, Duration lifetime, float cooldown, Optional<Effect> effect) {
        super(cooldown, effect);
        this.baseDamage = baseDamage;
        this.color = color;
        this.ignoreTags = ignoreTags;
        this.lifetime = lifetime;
        this.bulletSpeed = bulletSpeed;
    }

    @Override
    void forceFire(Vec2 position, Vec2 direction, Entity owner) {

        Vec2 velocity = direction.multiply(bulletSpeed);
        EntityOf<Bullet> bullet = BulletFactory.bullet(baseDamage, effect, position, velocity, color, owner, ignoreTags, lifetime, this);
        GameLoop.safeTrack(bullet);

        bullet.getMainSystem().onHit.listen(phy -> onHit.emit(phy), bullet);
    }
    
}
