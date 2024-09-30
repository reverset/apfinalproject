package game.core.rendering;

import game.Color;
import game.Vec2;
import game.ecs.Component;

import com.raylib.Raylib;

public class Triangle implements Component {

    public Vec2 position;

    public float width;
    public float height;

    Vec2 top;
    Vec2 left;
    Vec2 right;

    Color color;

    public Triangle(Vec2 center, float width, float height, Color color) {
        this.position = center;
        this.color = color;
        this.width = width;
        this.height = height;
        updatePoints();
    }
    
    public void updatePoints() {
        top = new Vec2(position.x, position.y-height*0.5f);
        left = new Vec2(position.x-width*0.5f, position.y+height*0.5f);
        right = new Vec2(position.x+width*0.5f, position.y+height*0.5f);
    }

    public void render() {
        Raylib.DrawTriangle(top.getPointer(), left.getPointer(), right.getPointer(), color.getPointer());
    }
}
