package game.core;

import java.util.Optional;

import game.Signal;
import game.Stopwatch;
import game.ecs.Component;

public class Health implements Component {
    private int health;
    private int maxHealth;
    private boolean confirmedDeath = false;

    @Deprecated
    public final Signal<Integer> onDamage = new Signal<>();
    
    public final Signal<DamageInfo> onDamageWithInfo = new Signal<>();
    public final Signal<Integer> onHeal = new Signal<>();
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


    public void heal(int life) {
        health += life;
        health = Math.min(maxHealth, health);
        onHeal.emit(life);
    }

    public Health withInvincibilityDuration(float dur) {
        invincibilityDuration = dur;
        return this;
    }

    @Deprecated
    public void damage(int dmg) {
        if (!invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)) return;

        health -= dmg;
        onDamage.emit(dmg);
        if (health <= 0 && !confirmedDeath) {
            confirmedDeath = true; 
            onDeath.emit(null);
        }
    }

    public DamageInfo damage(DamageInfo info) {
        if (!invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)) return DamageInfo.ofNone();
        
        DamageInfo inf = info;
        if (effect.isPresent()) inf = effect.get().computeDamageResistance(inf);

        int dmg = inf.damage();
        health -= dmg;
        onDamage.emit(dmg);
        onDamageWithInfo.emit(inf);
        if (health <= 0 && !confirmedDeath) {
            confirmedDeath = true; 
            onDeath.emit(null);
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
