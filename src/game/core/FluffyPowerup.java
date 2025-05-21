package game.core;

import java.time.Duration;

import game.GameLoop;
import game.Stopwatch;
import game.ecs.Entity;

public class FluffyPowerup extends Powerup {

    private static final Duration BUFF_DURATION = Duration.ofSeconds(5);

    private boolean isBuffActive = false;
    private Stopwatch buffUpdate = Stopwatch.ofGameTime();

    private Fluffy fluffy;

    public FluffyPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
        setPriority(10);
    }

    @Override
    public void ready() {
        super.ready();

        fluffy = GameLoop.safeTrack(Fluffy.makeEntity(entity, level)).getMainSystem();

        fluffy.onMiniModeChange.listen(mode -> {
            isBuffActive = mode;
            if (mode) {
                buffUpdate.restart();
            }
        }, entity);
    }

    @Override
    public DamageInfo outgoingDamageMod(DamageInfo info) {
        return super.outgoingDamageMod(info.conditionalDamageMod(() -> isBuffActive, i -> i.damage()*(Math.min(5, level+1))));
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
        fluffy.setLevel(level);
    }

    @Override
    public String getName() {
        return "Fluffy";
    }

    @Override
    public String getDescription() {
        return "A sheep... in space?";
    }
    
    @Override
    public String getSmallHUDInfo() {
        if (isBuffActive) {
            return (level+1) + "x damage.";
        }
        return "DEFENDING";
    }

    @Override
    public String getIconPath() {
        return "resources/fluffyicon.png";
    }
}
