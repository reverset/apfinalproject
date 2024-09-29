package game.core.rendering;

import game.ecs.ECSystem;

public abstract class Renderer extends ECSystem {
    abstract void hide();
    abstract void show();
    abstract boolean isHidden();
}
