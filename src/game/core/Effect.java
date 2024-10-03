package game.core;

import game.ecs.Component;

public class Effect implements Component {
    private int level = 1;

    public int getLevel() {
        return level;
    }

    public Effect setLevel(int l) {
        level = l;
        return this;
    }
}
