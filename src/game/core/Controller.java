package game.core;

import game.ecs.ECSystem;

import com.raylib.Raylib;
import com.raylib.Jaylib;


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
        if (Raylib.IsKeyPressed(Jaylib.KEY_A)) controlled.controlledLeftOnce();
        else if (Raylib.IsKeyDown(Jaylib.KEY_A)) controlled.controlledLeft();
        
        if (Raylib.IsKeyPressed(Jaylib.KEY_D)) controlled.controlledRightOnce();
        else if (Raylib.IsKeyDown(Jaylib.KEY_D)) controlled.controlledRight();
        
        if (Raylib.IsKeyPressed(Jaylib.KEY_S)) controlled.controlledDownOnce();
        else if (Raylib.IsKeyDown(Jaylib.KEY_S)) controlled.controlledDown();
        
        if (Raylib.IsKeyPressed(Jaylib.KEY_W)) controlled.controlledUpOnce();
        else if (Raylib.IsKeyDown(Jaylib.KEY_W)) controlled.controlledUp();

        if (Raylib.IsMouseButtonPressed(0)) controlled.controlledClickOnce();
        else if (Raylib.IsMouseButtonDown(0)) controlled.controlledClick();

    }
    
}
