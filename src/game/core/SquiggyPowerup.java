package game.core;

import game.EntityOf;
import game.GameLoop;
import game.Squiggy;
import game.ecs.Entity;

public class SquiggyPowerup extends Powerup {

    private EntityOf<Squiggy> squiggy = null;
    
    public SquiggyPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void ready() {
        super.ready();
        squiggy = GameLoop.safeTrack(Squiggy.makeEntity(entity, level));
    }

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public void doLevelUp() {
        level += 1;
        squiggy.getMainSystem().setLevel(level);
    }

    @Override
    public String getName() {
        return "Squiggy";
    }

    @Override
    public String getDescription() {
        return "A helpful companion.";
    }

    @Override
    public String getSmallHUDInfo() {
        return squiggy.getMainSystem().getState().toString() + (squiggy.getMainSystem().getState() == Squiggy.State.ATTACKING 
            ? " " + squiggy.getMainSystem().getTarget().get().getEntity().name.toUpperCase()
            : ""
            );
    }
    
}
