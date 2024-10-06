package game.core.rendering;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class PolyRenderer extends ECSystem {
    Poly poly;
    Transform trans;

    @Override
    public void setup() {
        poly = require(Poly.class);
        trans = require(Transform.class);
    }

    @Override
    public void render() {
        poly.render(trans.position, trans.rotation);
    }
    
}
