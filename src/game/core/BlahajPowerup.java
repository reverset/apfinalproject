package game.core;

import game.EntityOf;
import game.GameLoop;
import game.core.Blahaj.State;
import game.ecs.Entity;

public class BlahajPowerup extends Powerup {

    private EntityOf<Blahaj> blahaj = null;

    public BlahajPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void ready() {
        super.ready();
        blahaj = GameLoop.safeTrack(Blahaj.makeEntity(entity, level));
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
        blahaj.getMainSystem().setLevel(level);
    }

    @Override
    public String getName() {
        return "Blahaj";
    }

    @Override
    public String getDescription() {
        return "A friendly shark.";
    }
    

    @Override
    public String getSmallHUDInfo() {
        final var state = blahaj.getMainSystem().getState();
        return state + (state == State.ATTACKING ? " " + blahaj.getMainSystem().getTarget().get().getEntity().name.toUpperCase() : "");
    }

    @Override
    public String getIconPath() {
        return "resources/blahajicon.png";
    }
}
