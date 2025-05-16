package game.core;

import java.time.Duration;
import java.util.Optional;

import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class DecayPowerup extends Powerup {

    private static final int DAMAGE_INTERVAL_MILLIS = 500;

    public DecayPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
        setPriority(10);
    }

    @Override
    public DamageInfo incomingDamageMod(DamageInfo info) {
        if (info.isHealing()) return info;
        if (info.hasExtra("D.O.T")) return info;
        int total = info.damage();
        if (total == 0) return info;

        int desiredChunks = (int) Math.ceil(total * getPercent());
        if (total > 0 && desiredChunks == 0) desiredChunks = 1;

        // must be final because of the closure.
        final int chunks = desiredChunks;

        for (int i = 0; i < (int) (1 / getPercent()); i++) {
            GameLoop.runAfterGameTime(entity, Duration.ofMillis(DAMAGE_INTERVAL_MILLIS * (i+1)), () -> {
                info.victim().getComponent(Health.class).ifPresent(health -> {
                    GameLoop.defer(() -> {
                        Optional<Vec2> pos = info.victim()
                            .getComponent(Transform.class)
                            .map(t -> t.position.clone());
                        final var dmg = info
                            .setDamageAndColor(chunks, DamageColor.SPECIAL)
                            .setPosition(pos)
                            .setExtras(new Object[]{"D.O.T"});
                        health.damageBypassInvincibility(dmg);
                    });
                });
            });
        }
        return DamageInfo.ofNone();
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Decay";
    }

    @Override
    public String getDescription() {
        return "Instead of receiving damage\ninstantly, it is spread\nover time.";
    }

    @Override
    public String getSmallHUDInfo() {
        return ((int) (100 * getPercent())) + "% of damage/"+(DAMAGE_INTERVAL_MILLIS/1_000.0)+"sec";
    }

    private double getPercent() {
        return Math.pow(0.5, level+1);
    }

    @Override
    public String getIconPath() {
        return "resources/decay.png";
    }
}
