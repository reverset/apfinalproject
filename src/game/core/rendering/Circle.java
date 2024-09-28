package game.core.rendering;

import game.Color;
import game.Vec2;
import game.ecs.Component;

import com.raylib.Raylib;

public class Circle implements Component {
    
    float radius;
    Color color;

    public Circle(float radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    public void render(Vec2 pos) {
        Raylib.DrawCircle(pos.xInt(), pos.yInt(), radius, color.getPointer());
    }
}
