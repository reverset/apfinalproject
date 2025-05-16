package game.core;

import game.GameLoop;
import game.ecs.Entity;

public class FluffyPowerup extends Powerup {

    public FluffyPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void ready() {
        super.ready();

        GameLoop.safeTrack(Fluffy.makeEntity(entity, level));
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Fluffy";
    }

    @Override
    public String getDescription() {
        return "A sheep... in space?";
    }
    
}
