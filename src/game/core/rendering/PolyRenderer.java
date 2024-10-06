package game.core.rendering;

import java.util.Optional;

import game.Shader;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class PolyRenderer extends ECSystem {
    Poly poly;
    Transform trans;
    Optional<Shader> shader;


    @Override
    public void setup() {
        poly = require(Poly.class);
        trans = require(Transform.class);
        shader = optionallyRequire(Shader.class);
    }

    @Override
    public void render() {
        if (shader.isPresent()) {
            shader.get().with(() -> {
                poly.render(trans.position, trans.rotation);
            });
        } else {
            poly.render(trans.position, trans.rotation);
        }
    }
    
}
