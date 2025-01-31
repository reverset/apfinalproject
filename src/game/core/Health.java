package game.core;

import java.util.Optional;

import game.GameLoop;
import game.Signal;
import game.Stopwatch;
import game.ecs.Component;

public class Health implements Component {
    private int health;
    private int maxHealth;
    private boolean confirmedDeath = false;

    public final Signal<DamageInfo> onDamage = new Signal<>();
    public final Signal<DamageInfo> onHeal = new Signal<>();
    public final Signal<Void> onDeath = new Signal<>();

    private float invincibilityDuration = 0;
    private Optional<Effect> effect;

    private final Stopwatch invincibilityStopwatch = new Stopwatch();
    
    public Health(int maxHp, Optional<Effect> effect) {
        maxHealth = maxHp;
        health = maxHealth;
        this.effect = effect;
    }

    public Health(int maxHp, Effect effect) {
        this(maxHp, Optional.of(effect));
    }

    public Health(int maxHp) {
        this(maxHp, Optional.empty());
    }

    public Health withInvincibilityDuration(float dur) {
        invincibilityDuration = dur;
        return this;
    }

    public DamageInfo damageOrHeal(DamageInfo info) {
        return damageOrHeal(info, true);
    }

    public boolean isDead() {
        return health == 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isHealthSaturated() {
        return health >= maxHealth;
    }

    public DamageInfo heal(DamageInfo info) {
        if (info.isHarmful()) throw new IllegalArgumentException("Invoked heal() with a positive damageinfo.");
        return damageOrHeal(info);
    }

    public DamageInfo damage(DamageInfo info) {
        if (info.isHealing()) throw new IllegalArgumentException("Invoked damage() with a negative damageinfo.");
        return damageOrHeal(info);
    }

    /**
     * This method is used for dealing damage AND healing. The only differentiation is from
     * whether or not the damage is positive or negative.
    **/
    public DamageInfo damageOrHeal(DamageInfo info, boolean showNumbers) {
        if (isDead()) return DamageInfo.ofNone();
        if (info.isHarmful() && !invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)) return DamageInfo.ofNone();
        
        DamageInfo inf = info;
        if (effect.isPresent()) inf = effect.get().computeDamageResistance(inf);

        int dmg = inf.damage();
        setHealthWithinBounds(health - dmg);
        if (dmg >= 0) onDamage.emit(inf);
        else onHeal.emit(inf);

        if (health <= 0 && !confirmedDeath) {
            confirmedDeath = true; 
            onDeath.emit(null);
        }
        
        if (showNumbers) {
            final DamageInfo i = inf; // for the closure.
            inf.position().ifPresent(pos -> {
                GameLoop.safeTrack(DamageNumber.makeEntity(pos, i.absoluteDamageOrHeal(), i.damageColor()));
            });
        }
        return inf;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public Health setMaxHealth(int max) {
        maxHealth = max;
        return this;
    }

    public Health setMaxHealthAndHealth(int max) {
        maxHealth = max;
        health = maxHealth;
        return this;
    }

    public Health setHealth(int h) {
        health = h;
        return this;
    }

    public Health setHealthWithinBounds(int h) {
        health = h;
        if (health > maxHealth) health = maxHealth;
        if (health < 0) health = 0;
        return this;
    }

    public Health maximizeHealth() {
        return setHealth(maxHealth);
    }

    public Health kill() {
        health = 0;
        onDeath.emit(null);
        confirmedDeath = true;
        return this;
    }

    public float getHealthPercentage() {
        return (float)health / (float) maxHealth;
    }

    public boolean isCritical() {
        return getHealthPercentage() < 0.25;
    }
    
}
