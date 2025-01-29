package game.core;


import java.time.Duration;

import game.GameTimeStopwatch;
import game.RecoverableException;
import game.ecs.Entity;

public class HealthRegenPowerup extends Powerup {
    private static final Duration HEALTH_RATE = Duration.ofMillis(5_000);
    private static final int BASE_HEALTH = 10;
    private Health health;
    private GameTimeStopwatch stopwatch = new GameTimeStopwatch();
    private boolean initialized = false;

    public HealthRegenPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void infrequentUpdate() {
        if (entity == null) return;
        if (!initialized && entity != null) {
            health = entity.getComponent(Health.class).orElseThrow(() -> new RecoverableException());
            initialized = true;
        }

        float delta = infreqDelta();
        stopwatch.tick(delta * 1_000.0f); // FIXME TODO!!!

        if (stopwatch.hasElapsedAdvance(HealthRegenPowerup.HEALTH_RATE)) {
            health.heal(level * HealthRegenPowerup.BASE_HEALTH);
        }
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public void levelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Health Regen";
    }

    @Override
    public String getDescription() {
        return "Passively regenerate health over time.";
    }
}
