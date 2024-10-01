package game.core;

import java.util.function.Supplier;

import game.EntityOf;
import game.GameLoop;
import game.Signal;
import game.Stopwatch;
import game.Tuple;
import game.Vec2;

public class Weapon {
    
    // First is bullet that hit the entity, second is the Physics system of the entity that was hit.
    public Signal<Tuple<Bullet, Physics>> onHit = new Signal<>();

    Stopwatch coolDownStopwatch = new Stopwatch();
    Supplier<EntityOf<Bullet>> bulletSupplier;
    
    float cooldown;
    float speed;
    int damage;

    public Weapon(Supplier<EntityOf<Bullet>> bulletSupplier, float cooldown, float speed) {
        this.bulletSupplier = bulletSupplier;
        this.cooldown = cooldown;
        this.speed = speed;
        this.damage = Bullet.BULLET_DAMAGE;
    }

    public Weapon setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public Weapon setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public Weapon setCooldown(float cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public boolean canFire() {
        return coolDownStopwatch.hasElapsedSeconds(cooldown);
    }

    public int getDamage() {
        return damage;
    }

    public void forceFire(Vec2 position, Vec2 direction) {
        EntityOf<Bullet> bullet = bulletSupplier.get();
        Bullet sys = bullet.getMainSystem();
        
        sys.damage = damage;
        sys.trans.position = position.clone();
        sys.tangible.velocity = direction.multiply(speed);
        
        sys.onHit.listen(phy -> {
            onHit.emit(new Tuple<>(sys, phy));
        }, sys.entity);

        GameLoop.safeTrack(bullet);
    }

    public void fire(Vec2 position, Vec2 direction) {
        if (coolDownStopwatch.hasElapsedSecondsAdvance(cooldown)) {
            forceFire(position, direction);
        }
    }
}
