package game.core;

import java.util.Objects;
import java.util.Optional;

import game.Signal;
import game.Stopwatch;
import game.Vec2;
import game.ecs.Entity;

public abstract class Weapon2 { // old weapon system was terrible, this is attempt 2.
    
    public final Signal<Physics> onHit = new Signal<>();

    float cooldown;

    Stopwatch shootTimer = Stopwatch.ofGameTime();
    Optional<Effect> effect;
    
    public Weapon2(float cooldown, Optional<Effect> effect) {
        this.cooldown = cooldown;
        this.effect = effect;
    }

    abstract void forceFire(Vec2 position, Vec2 direction, Entity owner);

    public void fire(Vec2 position, Vec2 direction, Entity owner) {
        if (shootTimer.hasElapsedSecondsAdvance(cooldown)) {
            forceFire(position, direction, owner);
        }
    }

    public boolean canFire() {
        return shootTimer.hasElapsedSeconds(cooldown);
    }

    public DamageInfo getDesiredDamage(DamageInfo info) {
        return effect.isPresent()
            ? effect.get().computeDamage(info)
            : info;
    }

    public void render() {}

    public Weapon2 setEffect(Effect effect) {
        Objects.requireNonNull(effect);
        this.effect = Optional.of(effect);
        return this;
    }
}
