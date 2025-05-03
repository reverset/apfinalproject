package game.core;

import game.ecs.ECSystem;

public class AutoTeamRegister extends ECSystem {

    @Override
    public void setup() {
    }

    @Override
    public void ready() {
        Team.getTeamByTagOf(entity).registerMember(entity, true);
    }
    
}
