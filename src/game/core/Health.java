package game.core;

import java.util.Optional;

import game.GameLoop;
import game.RecoverableException;
import game.Signal;
import game.Stopwatch;
import game.ecs.Component;
import game.ecs.Entity;

public class Health implements Component {
    private int health;
    private int maxHealth;
    private boolean confirmedDeath = false;

    public final Signal<DamageInfo> onDamage = new Signal<>();
    public final Signal<DamageInfo> onHeal = new Signal<>();
    public final Signal<Void> onDeath = new Signal<>();
    public final Signal<Entity> onDeathByAttacker = new Signal<>();

    private float invincibilityDuration = 0;
    private Optional<Effect> effect;

    private final Stopwatch invincibilityStopwatch = Stopwatch.ofGameTime();
    private boolean isInvincible = false;

    public static Health ofInvincible() {
        Health h = new Health(Integer.MAX_VALUE);
        h.isInvincible = true;
        return h;
    }

    public Health(int maxHp, Optional<Effect> effect) {
        maxHealth = maxHp;
        health = maxHealth;
        this.effect = effect;
        invincibilityStopwatch.start();
    }

    public Health(int maxHp, Effect effect) {
        this(maxHp, Optional.of(effect));
    }

    public Health(double maxHp, Effect effect) {
        this((int)maxHp, Optional.of(effect));
    }

    public Health(int maxHp) {
        this(maxHp, Optional.empty());
    }

    public Health withInvincibilityDuration(float dur) {
        invincibilityDuration = dur;
        return this;
    }

    public DamageInfo damageOrHeal(DamageInfo info) {
        return damageOrHeal(info, true, false);
    }

    public boolean isDead() {
        return health == 0 && confirmedDeath;
    }

    public boolean isAlive() {
        return health > 0 && !confirmedDeath;
    }

    public boolean isHealthSaturated() {
        return health >= maxHealth;
    }

    public DamageInfo heal(DamageInfo info) {
        if (info.isHarmful()) throw new RecoverableException("Invoked heal() with a positive damageinfo.");
        return damageOrHeal(info);
    }

    public DamageInfo damage(DamageInfo info) {
        if (info.isHealing()) throw new RecoverableException("Invoked damage() with a negative damageinfo.");
        return damageOrHeal(info);
    }

    public DamageInfo damageBypassInvincibility(DamageInfo info) {
        if (info.isHealing()) throw new RecoverableException("Invoked damage() with a negative damageinfo.");
        return damageOrHeal(info, true, true);
    }

    public void setInvincible(boolean inv) {
        isInvincible = inv;
    }

    /**
     * This method is used for dealing damage AND healing. The only differentiation is from
     * whether or not the damage is positive or negative.
    **/
    public DamageInfo damageOrHeal(DamageInfo info, boolean showNumbers, boolean bypassInvincibility) {
        if (isDead()) return DamageInfo.ofNone();
        if (isInvincible // huge if
            || (invincibilityDuration != 0 
                && info.isHarmful() 
                && !bypassInvincibility 
                && !invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)
            )) return DamageInfo.ofNone();
        
        DamageInfo inf = info;
        if (inf.isHarmful() && effect.isPresent()) inf = effect.get().computeDamageResistance(inf);

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
                GameLoop.safeTrack(DamageNumber.makeEntity(pos, i.absoluteDamageOrHeal(), getMaxHealth(), i.damageColor()));
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

    public void revive() {
        if (!confirmedDeath) return;

        health = maxHealth;
        confirmedDeath = false;
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

    public boolean isLessThanHalf() {
        return getHealthPercentage() < 0.5;
    }
    
}
