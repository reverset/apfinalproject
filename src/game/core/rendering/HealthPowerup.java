package game.core.rendering;

import game.core.Effect;
import game.core.Health;
import game.core.Powerup;
import game.core.Weapon2;
import game.ecs.Entity;

public class HealthPowerup extends Powerup {
    public static final int HEALTH_BONUS = 30;

    private int currentHealthBonus = 30;
    private int initialHealth = 0;
    
    private Health health;

    public HealthPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        health = require(Health.class);
        initialHealth = health.getMaxHealth();

        health.setMaxHealth(initialHealth + currentHealthBonus);
    }

    @Override
    public int getMaxLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void levelUp() {
        currentHealthBonus += HEALTH_BONUS;
        health.setMaxHealth(initialHealth + currentHealthBonus);
    }

    @Override
    public String getName() {
        return "Health Bonus";
    }

    @Override
    public String getDescription() {
        return "+" + HEALTH_BONUS + " Max Health";
    }
    
}
