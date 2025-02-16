package game.core.rendering;

import game.core.Effect;
import game.core.Health;
import game.core.Powerup;
import game.core.Weapon2;
import game.ecs.Entity;

public class HealthPowerup extends Powerup {
    public static final int HEALTH_BONUS = 100;

    private Health health;

    public HealthPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        health = require(Health.class);

        boolean wasAtFull = health.getHealth() >= health.getMaxHealth();
        health.setMaxHealth(health.getMaxHealth() + HEALTH_BONUS);
        if (wasAtFull) {
            health.setHealth(health.getMaxHealth());
        }
    }

    @Override
    public int getMaxLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void levelUp() {
        health.setMaxHealth(health.getMaxHealth() + HEALTH_BONUS);
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
