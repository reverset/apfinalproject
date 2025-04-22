package game.core;


import java.time.Duration;

import game.RecoverableException;
import game.Stopwatch;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class HealthRegenPowerup extends Powerup {
    private static final Duration HEALTH_RATE = Duration.ofMillis(3_000);
    private static final int BASE_HEALTH = 15;
    private Health health;
    private Stopwatch stopwatch = Stopwatch.ofGameTime();
    private boolean initialized = false;

    public HealthRegenPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    public int calculateDesiredHealth() {
        return level * HealthRegenPowerup.BASE_HEALTH;
    }

    @Override
    public void infrequentUpdate() {
        if (entity == null) return;
        if (!initialized && entity != null) {
            health = entity.getComponent(Health.class).orElseThrow(() -> new RecoverableException());
            initialized = true;
            stopwatch.start();
        }

        if (stopwatch.hasElapsedAdvance(HealthRegenPowerup.HEALTH_RATE) && !health.isHealthSaturated()) {
            var position = entity
                .getComponent(Transform.class)
                .map(trans -> trans.position.clone());

            var info = new DamageInfo(-calculateDesiredHealth(), entity, null)
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

    @Override
    public String getSmallHUDInfo() {
        return "+" + calculateDesiredHealth() + "hp" + "/" + HEALTH_RATE.toSeconds() + "sec";
    }
}
