package game.core;

import java.util.function.Supplier;

import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;

public class Weapon {
    
    Stopwatch coolDownStopwatch = new Stopwatch();
    Supplier<EntityOf<Bullet>> bulletSupplier;
    
    float cooldown;
    float speed;

    public Weapon(Supplier<EntityOf<Bullet>> bulletSupplier, float cooldown, float speed) {
        this.bulletSupplier = bulletSupplier;
        this.cooldown = cooldown;
        this.speed = speed;
    }

    public Weapon setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public Weapon setCooldown(float cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public boolean canFire() {
        return coolDownStopwatch.hasElapsedSeconds(cooldown);
    }

    public void fire(Vec2 position, Vec2 direction) {
        if (coolDownStopwatch.hasElapsedSecondsAdvance(cooldown)) {
            EntityOf<Bullet> bullet = bulletSupplier.get();
            Bullet sys = bullet.getMainSystem();
            
            sys.trans.position = position;
            sys.tangible.velocity = direction.multiply(speed);

            GameLoop.safeTrack(bullet);
        }
    }
}
