package game.core;

import java.util.function.Supplier;

import game.EntityOf;
import game.GameLoop;
import game.Vec2;

public class RadiusWeapon extends Weapon {
    float radianPerBullet;
    public RadiusWeapon(Supplier<EntityOf<Bullet>> bulletSupplier, float cooldown, float speed, float degreePerBullet) {
        super(bulletSupplier, cooldown, speed);
        this.radianPerBullet = (float) Math.toRadians(degreePerBullet);
    }

    public RadiusWeapon setDegreePerBullet(float degree) {
        radianPerBullet = (float) Math.toRadians(degree);
        return this;
    }

    @Override
    public void fire(Vec2 position, Vec2 direction) {
        if (coolDownStopwatch.hasElapsedSecondsAdvance(cooldown)) {
            for (float rad = 0; rad < Math.PI*2; rad += radianPerBullet) {
                Vec2 dir = Vec2.fromAngle(rad);

                EntityOf<Bullet> bullet = bulletSupplier.get();
                Bullet sys = bullet.getMainSystem();
                sys.trans.position = position.clone();
                sys.tangible.velocity = dir.multiplyEq(speed);

                GameLoop.safeTrack(bullet);
            }
        }
    }
}
