package game.core.rendering;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class CircleRenderer extends ECSystem {

    Circle circle;
    Transform trans;

    @Override
    public void setup() {
        circle = require(Circle.class);
        trans = require(Transform.class);
    }

    @Override
    public void render() {
        circle.render(trans.position);
    }
    
}
