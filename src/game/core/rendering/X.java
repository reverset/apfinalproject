package game.core.rendering;

import game.Color;
import game.Vec2;
import game.ecs.Component;

import com.raylib.Raylib;

public class X implements Component {
    private Vec2 position;
    private Vec2 topLeft;
    private Vec2 topRight;
    private Vec2 bottomLeft;
    private Vec2 bottomRight;

    private float thickness;
    private Color color;
    private float length;

    public X(Vec2 position, Color color, float thickness, float length) {
        this.color = color;
        this.thickness = thickness;
        this.length = length;
        setPosition(position);
    }

    public X setPosition(Vec2 pos) {
        position = pos;

        topLeft = position.minus(length*0.5f);
        topRight = position.add(length*0.5f, -length*0.5f);
        bottomLeft = position.add(-length*0.5f, length*0.5f);
        bottomRight = position.add(length*0.5f);

        return this;
    }

    public X setLength(float length) {
        this.length = length;
        return setPosition(position);
    }

    public void render() {
        Raylib.DrawLineEx(topLeft.asCanonicalVector2(), bottomRight.asCanonicalVector2(), thickness, color.getPointer());
        Raylib.DrawLineEx(topRight.asCanonicalVector2(), bottomLeft.asCanonicalVector2(), thickness, color.getPointer());
    }
}
