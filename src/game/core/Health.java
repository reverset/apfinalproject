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


    public void heal(int life) { // rework soon
        health += life;
        health = Math.min(maxHealth, health);
        onHeal.emit(life);
    }

    public Health withInvincibilityDuration(float dur) {
        invincibilityDuration = dur;
        return this;
    }

    public DamageInfo damage(DamageInfo info) {
        return damage(info, true);
    }

    public boolean isDead() {
        return health == 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public DamageInfo damage(DamageInfo info, boolean showNumbers) {
        if (isDead()) return DamageInfo.ofNone();
        if (!invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)) return DamageInfo.ofNone();
        
        DamageInfo inf = info;
        if (effect.isPresent()) inf = effect.get().computeDamageResistance(inf);

        int dmg = inf.damage();
        health -= dmg;
        onDamage.emit(inf);
        if (health <= 0 && !confirmedDeath) {
            confirmedDeath = true; 
            onDeath.emit(null);
        }
        
        if (showNumbers) {
            final DamageInfo i = inf; // for the closure.
            inf.position().ifPresent(pos -> {
                GameLoop.safeTrack(DamageNumber.makeEntity(pos, i.damage(), i.damageColor()));
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
