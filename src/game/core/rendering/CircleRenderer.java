package game.core.rendering;

import java.util.Optional;

import game.Shader;
import game.ecs.comps.Transform;

public class CircleRenderer extends Renderer {

    Circle circle;
    Transform trans;
    Optional<Shader> shader;

    private boolean hidden = false;

    @Override
    public void setup() {
        circle = require(Circle.class);
        trans = require(Transform.class);
        shader = optionallyRequire(Shader.class);
    }

    @Override
    public void render() {
        if (hidden) return;

        if (shader.isPresent()) {
            Shader shade = shader.get();
            shade.activate();
            circle.render(trans.position);
            shade.deactivate();
        } else {
            circle.render(trans.position);
        }
    }

    @Override
    public void hide() {
        hidden = true;
    }

    @Override
    public void show() {
        hidden = false;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }
    
}
