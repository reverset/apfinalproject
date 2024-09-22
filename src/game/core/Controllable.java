package game.core;

import game.Vec2;

import com.raylib.Raylib;
import com.raylib.Jaylib;


public interface Controllable {
    default void controlledLeft() {};
    default void controlledLeftOnce() {};
    
    default void controlledRight() {};
    default void controlledRightOnce() {};
    
    default void controlledUp() {};
    default void controlledUpOnce() {};

    default void controlledDown() {};
    default void controlledDownOnce() {};

    default void controlledClick() {};
    default void controlledClickOnce() {};

    default Vec2 controlledMoveVector() {
        float x = (Raylib.IsKeyDown(Jaylib.KEY_D) ? 1.0f : 0.0f) - (Raylib.IsKeyDown(Jaylib.KEY_A) ? 1.0f : 0.0f);
        float y = (Raylib.IsKeyDown(Jaylib.KEY_S) ? 1.0f : 0.0f) - (Raylib.IsKeyDown(Jaylib.KEY_W) ? 1.0f : 0.0f);
        return new Vec2(x, y).normalizeEq();
    }
}
