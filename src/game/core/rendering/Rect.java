package game.core.rendering;

import game.Color;
import game.Vec2;
import game.ecs.Component;

import com.raylib.Raylib;

public class Rect implements Component {
    public int width;
    public int height;
    public Color color;
    
    public Rect(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }
    
    public boolean overlaps(Vec2 position, Vec2 otherPosition, Rect other) {
        var rect = raylibFromPosition(position);
        var otherR = other.raylibFromPosition(otherPosition);
        boolean result = Raylib.CheckCollisionRecs(rect, otherR);

        rect.close();
        otherR.close();

        return result;
    }

    public Vec2 getCenter(Vec2 position) { // Position is the top left corner.
        return new Vec2(position.x + width*0.5f, position.y + height*0.5f);
    }

    public void render(Vec2 position) {
        Raylib.DrawRectangle(position.xInt(), position.yInt(), width, height, color.getPointer());
    }

    public void renderRound(Vec2 position, float roundness, int segments) {
        var rec = raylibFromPosition(position);
        Raylib.DrawRectangleRounded(rec, roundness, segments, color.getPointer());
        rec.close();
    }

    protected Raylib.Rectangle raylibFromPosition(Vec2 pos) {
        return new Raylib.Rectangle().x(pos.x).y(pos.y).width(width).height(height);
    }
}
