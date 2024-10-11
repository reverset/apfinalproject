package game.core.rendering;

import game.ecs.ECSystem;

public class XRenderer extends ECSystem {

    X x;

    @Override
    public void setup() {
        x = require(X.class);
    }

    @Override
    public void render() {
        x.render();
    }
    
}
