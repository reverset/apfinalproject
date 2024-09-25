package game.core;

import game.Signal;
import game.ecs.Component;

public class Health implements Component {
    private int health;
    private int maxHealth;

    public Health(int maxHp) {
        maxHealth = maxHp;
        health = maxHealth;
    }

    public final Signal<Integer> onDamage = new Signal<>();
    public final Signal<Void> onDeath = new Signal<>();

    public void damage(int dmg) {
        health -= dmg;
        onDamage.emit(dmg);
        if (health <= 0) onDeath.emit(null);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercentage() {
        return (float)health / (float) maxHealth;
    }

    // Returns true if health is less than 15%
    public boolean isCritical() {
        return getHealthPercentage() < 0.15;
    }
    
}
