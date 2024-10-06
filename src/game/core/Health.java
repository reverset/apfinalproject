package game.core;

import game.Signal;
import game.Stopwatch;
import game.ecs.Component;

public class Health implements Component {
    private int health;
    private int maxHealth;
    private boolean confirmedDeath = false;

    public final Signal<Integer> onDamage = new Signal<>();
    public final Signal<Integer> onHeal = new Signal<>();
    public final Signal<Void> onDeath = new Signal<>();

    private float invincibilityDuration = 0;

    private final Stopwatch invincibilityStopwatch = new Stopwatch();
    
    public Health(int maxHp) {
        maxHealth = maxHp;
        health = maxHealth;
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

    public void damage(int dmg) { // TODO: make this function return the amount of damage actually done.
        if (!invincibilityStopwatch.hasElapsedSecondsAdvance(invincibilityDuration)) return;

        health -= dmg;
        onDamage.emit(dmg);
        if (health <= 0 && !confirmedDeath) {
            confirmedDeath = true; 
            onDeath.emit(null);
        }
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
