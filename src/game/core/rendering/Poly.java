package game.core.rendering;

import game.Color;
import game.Shader;
import game.Vec2;
import game.ecs.Component;

import java.util.Optional;

import com.raylib.Raylib;

public class Poly implements Component {
    public Color color;
    int sides;
    float radius;

    public Poly(int sides, float radius, Color color) {
        this.sides = sides;
        this.radius = radius;
        this.color = color;
    }

    public void render(Vec2 center, float rotation) {
        Raylib.DrawPoly(center.asCanonicalVector2(), sides, radius, rotation, color.getPointer());
    }
}
