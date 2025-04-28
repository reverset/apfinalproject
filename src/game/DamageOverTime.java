package game;

import java.time.Duration;
import java.util.Optional;

import game.core.DamageColor;
import game.core.DamageInfo;
import game.core.Effect;
import game.core.Health;
import game.core.Powerup;
import game.core.Weapon2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class DamageOverTime extends Powerup {

    private static final int DAMAGE_INTERVAL_MILLIS = 500;

    public DamageOverTime(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    // TODO: add ordering, this should always happen after all other Powerups.
    @Override
    public DamageInfo incomingDamageMod(DamageInfo info) {
        if (info.isHealing()) return info;
        if (info.hasExtra("D.O.T")) return info;
        int total = info.damage();

        int desiredChunks = (int) Math.ceil(total * getPercent());
        if (total > 0 && desiredChunks == 0) desiredChunks = 1;

        // must be final because of the closure.
        final int chunks = desiredChunks;

        for (int i = 0; i < (int) (1 / getPercent()); i++) {
            GameLoop.runAfter(entity, Duration.ofMillis(DAMAGE_INTERVAL_MILLIS * (i+1)), () -> {
                info.victim().getComponent(Health.class).ifPresent(health -> {
                    GameLoop.defer(() -> {
                        Optional<Vec2> pos = info.victim()
                            .getComponent(Transform.class)
                            .map(t -> t.position.clone());
                        final var dmg = info
                            .setDamageAndColor(chunks, DamageColor.SPECIAL)
                            .setPosition(pos)
                            .setExtras(new Object[]{"D.O.T"});
                        health.damage(dmg);
                    });
                });
            });
        }
        return DamageInfo.ofNone();
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public void levelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Damage Over Time";
    }

    @Override
    public String getDescription() {
        return "Instead of recieving damage\ninstantly, it is spread\nover time.";
    }

    @Override
    public String getSmallHUDInfo() {
        return ((int) (100 * getPercent())) + "% of damage/"+(DAMAGE_INTERVAL_MILLIS/1_000.0)+"sec";
    }

    private double getPercent() {
        return Math.pow(0.5, level);
    }
    
}
