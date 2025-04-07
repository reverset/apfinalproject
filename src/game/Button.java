package game;

import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import com.raylib.Raylib;

public class Button extends ECSystem {

    protected Rect rect;
    protected Transform trans;

    protected Runnable callback = () -> {};

    public Button(Runnable callback) {
        this.callback = callback;
    }

    public Button() {}

    @Override
    public void setup() {
        rect = require(Rect.class);
        trans = require(Transform.class);
    }

    @Override
    public void frame() {
        if (entity.isHidden()) return;
        
        Vec2 mousePos = GameLoop.getMouseScreenPosition();
        if (Raylib.IsMouseButtonPressed(0) && rect.pointWithin(trans.position, mousePos)) {
            onClick();
        }
    }

    private void onClick() {
        callback.run();
    }
    
}
