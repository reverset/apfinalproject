package game;

import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import com.raylib.Raylib;

public class Button extends ECSystem {

    protected Rect rect;
    protected Transform trans;
    private boolean centered;

    protected Runnable callback = () -> {};

    public Button(Runnable callback) {
        this(callback, false);
    }

    public Button(Runnable callback, boolean centered) {
        this.callback = callback;
        this.centered = centered;
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
        Vec2 c = trans.position;
        if (centered) c = rect.centerize(trans.position);
        if (Raylib.IsMouseButtonPressed(0) && rect.pointWithin(c, mousePos)) {
            onClick();
        }
    }

    private void onClick() {
        callback.run();
    }
    
}
