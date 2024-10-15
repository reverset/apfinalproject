package game.core.rendering;

import game.Text;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class HUDTextRenderer extends ECSystem {
    Transform trans;
    Text text;

    public HUDTextRenderer(Text text) {
        this.text = text;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public void hudRender() {
        text.position = trans.position;
        text.render();
    }
    
}
