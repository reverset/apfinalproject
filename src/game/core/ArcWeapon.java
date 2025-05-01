package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public class ArcWeapon extends Weapon2 {
    private int baseDmgPerPellet;
    private float bulletSpeed;

    private int pellets;
    private float arcRadians;
    private Color color;
    private Object[] ignoreTags;
    private Duration lifetime;

    public ArcWeapon(int baseDmgPerPellet, float arcRadians, int pellets, float bulletSpeed, Color color, Object[] ignoreTags, float cooldown, Duration lifetime, Optional<Effect> effect) {
        super(cooldown, effect);
        this.baseDmgPerPellet = baseDmgPerPellet;
        this.arcRadians = arcRadians;
        this.color = color;
        this.ignoreTags = ignoreTags;
        this.lifetime = lifetime;
        this.pellets = pellets;
        this.bulletSpeed = bulletSpeed;
    }

    @Override
    void forceFire(Vec2 position, Vec2 direction, Entity owner) {
        float angle = direction.getAngle();
        // float range = angle + ((float)Math.PI/4) - (angle - ((float)Math.PI/4));

        for (float rad = angle - (arcRadians/2); rad < angle + (arcRadians/2); rad += (arcRadians/pellets)) {
            Vec2 dir = Vec2.fromAngle(rad);

            EntityOf<Bullet> bullet = BulletFactory.bullet(baseDmgPerPellet, effect, position.clone(), dir.multiplyEq(bulletSpeed), color, owner, ignoreTags, lifetime, this);
            GameLoop.safeTrack(bullet);
        }
    }
    
}
