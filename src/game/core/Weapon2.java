package game.core;

import java.util.Objects;
import java.util.Optional;

import game.Color;
import game.Signal;
import game.Stopwatch;
import game.Vec2;
import game.ecs.Entity;

public abstract class Weapon2 { // old weapon system was terrible, this is attempt 2.
    
    public final Signal<Physics> onHit = new Signal<>();

    protected float cooldown;

    protected Stopwatch shootTimer = Stopwatch.ofGameTime();
    protected Optional<Effect> effect;

    protected Object[] ignoreTags = {};

    private Color hitMarkerColor = Color.WHITE;
    
    public Weapon2(float cooldown, Optional<Effect> effect) {
        this.cooldown = cooldown;
        this.effect = effect;
    }

    public void setIgnoreTags(Object[] ignoreTags) {
        this.ignoreTags = ignoreTags;
    }
    
    abstract void forceFire(Vec2 position, Vec2 direction, Entity owner);
    
    public Color getHitMakerColor() {
        return hitMarkerColor;
    }

    public void setHitMarkerColor(Color color) {
        hitMarkerColor = Objects.requireNonNull(color);
    }

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
