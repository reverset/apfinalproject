package game;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class DespawnDistance extends ECSystem {

    private float distance;
    private Transform trackedTrans;

    public DespawnDistance(Transform trackedTrans, float distance) {
        this.distance = distance;
        this.trackedTrans = trackedTrans;
    }

    private Transform trans;

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public void infrequentUpdate() {
        if (trans.position.distance(trackedTrans.position) > distance) {
            GameLoop.safeDestroy(entity);
        }
    }

}
