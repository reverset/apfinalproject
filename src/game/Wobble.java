package game;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class Wobble extends ECSystem {

    private Transform trans;

    private float sizeCoeff;
    private float speedCoeff;

    public Wobble(float sizeCoeff, float speedCoeff) {
        this.sizeCoeff = sizeCoeff;
        this.speedCoeff = speedCoeff;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public void frame() {
        trans.position.x = (float) (Math.cos(speedCoeff * GameLoop.getUnpausedTime()) * sizeCoeff);
    }
    
}
