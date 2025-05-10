package game.core.rendering;

import java.util.Optional;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class XRenderer extends ECSystem {

    private X x;
    private Optional<Transform> trans;

    @Override
    public void setup() {
        x = require(X.class);
        trans = optionallyRequire(Transform.class);
    }

    @Override
    public void render() {
        trans.ifPresent(t -> x.setPosition(t.position.clone()));
        x.render();
    }
    
}
