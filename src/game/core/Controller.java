package game.core;

import game.ecs.ECSystem;

import com.raylib.Raylib;

public class Controller<T extends ECSystem & Controllable> extends ECSystem {
    private T controlled = null;
    private Class<T> controlledClass = null;

    public Controller(T controlled) {
        this.controlled = controlled;
    }

    public Controller(Class<T> controlledClass) {
        this.controlledClass = controlledClass;
    }

    @Override
    public void setup() {
        if (controlled == null) {
            controlled = requireSystem(controlledClass);
        }
    }

    @Override
    public void frame() {
        if (Raylib.IsKeyPressed(Raylib.KEY_A)) controlled.controlledLeftOnce();
        else if (Raylib.IsKeyDown(Raylib.KEY_A)) controlled.controlledLeft();
        
        if (Raylib.IsKeyPressed(Raylib.KEY_D)) controlled.controlledRightOnce();
        else if (Raylib.IsKeyDown(Raylib.KEY_D)) controlled.controlledRight();
        
        if (Raylib.IsKeyPressed(Raylib.KEY_S)) controlled.controlledDownOnce();
        else if (Raylib.IsKeyDown(Raylib.KEY_S)) controlled.controlledDown();
        
        if (Raylib.IsKeyPressed(Raylib.KEY_W)) controlled.controlledUpOnce();
        else if (Raylib.IsKeyDown(Raylib.KEY_W)) controlled.controlledUp();

        if (Raylib.IsMouseButtonPressed(0)) controlled.controlledClickOnce();
        else if (Raylib.IsMouseButtonDown(0)) controlled.controlledClick();

    }
    
}
