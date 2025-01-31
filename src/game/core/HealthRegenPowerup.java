package game.core;


import java.time.Duration;

import game.GameTimeStopwatch;
import game.RecoverableException;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class HealthRegenPowerup extends Powerup {
    private static final Duration HEALTH_RATE = Duration.ofMillis(3_000);
    private static final int BASE_HEALTH = 5;
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
            stopwatch.start();
        }

        float delta = infreqDelta();
        stopwatch.tick(delta * 1_000.0f);

        if (stopwatch.hasElapsedAdvance(HealthRegenPowerup.HEALTH_RATE) && !health.isHealthSaturated()) {
            var position = entity
                .getComponent(Transform.class)
                .map(trans -> trans.position.clone());

            var info = new DamageInfo(-(level * HealthRegenPowerup.BASE_HEALTH), entity, null)
                .setPosition(position);
            // health.heal(level * HealthRegenPowerup.BASE_HEALTH);
            health.heal(info);
        }
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public void levelUp() {
        System.out.println("Health regen level up.");
        level += 1;
    }

    @Override
    public String getName() {
        return "Health Regen";
    }

    @Override
    public String getDescription() {
        return "Passively regenerate \nhealth over time.";
    }
}
