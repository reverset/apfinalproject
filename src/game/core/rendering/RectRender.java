package game.core.rendering;

import game.Shader;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import java.util.Optional;

import com.raylib.Raylib;

public class RectRender extends ECSystem {
    private Rect rect;
    private Transform trans;
    private Optional<Shader> shader;

    @Override
    public void setup() {
        rect = require(Rect.class);
        trans = require(Transform.class);
        shader = optionallyRequire(Shader.class);
    }

    @Override
    public void render() {
        if (shader.isPresent()) {
            Shader shade = shader.get();
            shade.activate();
            Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
            shade.deactivate();
        } else {
            Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
        }
    }
    
}
