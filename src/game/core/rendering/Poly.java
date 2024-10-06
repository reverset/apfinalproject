package game.core.rendering;

import game.Color;
import game.Vec2;
import game.ecs.Component;
import com.raylib.Raylib;

public class Poly implements Component {
    int sides;
    float radius;
    Color color;

    public Poly(int sides, float radius, Color color) {
        this.sides = sides;
        this.radius = radius;
        this.color = color;
    }

    public void render(Vec2 center, float rotation) {
        Raylib.DrawPoly(center.getPointer(), sides, radius, rotation, color.getPointer());
    }
}
