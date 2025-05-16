package game.core.rendering;

import java.util.Optional;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class XRenderer extends ECSystem {

    private X x;
    private Optional<Transform> trans;
    private boolean enabled = true;

    @Override
    public void setup() {
        x = require(X.class);
        trans = optionallyRequire(Transform.class);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void render() {
        if (!enabled) return;

        trans.ifPresent(t -> x.setPosition(t.position.clone()));
        x.render();
    }
    
}
