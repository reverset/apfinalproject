package game.core.rendering;

import java.util.Optional;

import game.Shader;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class CircleRenderer extends ECSystem {

    Circle circle;
    Transform trans;
    Optional<Shader> shader;

    @Override
    public void setup() {
        circle = require(Circle.class);
        trans = require(Transform.class);
        shader = optionallyRequire(Shader.class);
    }

    @Override
    public void render() {
        if (shader.isPresent()) {
            Shader shade = shader.get();
            shade.activate();
            circle.render(trans.position);
            shade.deactivate();
        } else {
            circle.render(trans.position);
        }
    }
    
}
