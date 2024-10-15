package game.core.rendering;

import game.Color;
import game.Janitor;
import game.Vec2;
import game.core.Ray;
import game.ecs.Component;

import java.util.Optional;

import com.raylib.Raylib;

public class Rect implements Component {
    public int width;
    public int height;
    public Color color;

    private Raylib.Rectangle internal;
    private Vec2 center = Vec2.zero();

    public Rect(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
        internal = raylibFromPosition(Vec2.zero());

        Janitor.registerAsyncSafe(this, internal::close);
    }

    public static Rect around(Circle circle) {
        return new Rect((int) (circle.radius*2), (int) (circle.radius*2), circle.color);
    }

    public static Rect around(float diameter, Color color) {
        return new Rect((int) diameter, (int) diameter, color);
    }
    
    public boolean overlaps(float x, float y, float ox, float oy, Rect other) {
        internal.x(x).y(y);

        var otherPointer = other.getPointer().x(ox).y(oy);
        return Raylib.CheckCollisionRecs(internal, otherPointer);
    }

    public boolean overlaps(Vec2 position, Vec2 otherPosition, Rect other) {
        internal.x(position.x).y(position.y);

        var otherPointer = other.getPointer().x(otherPosition.x).y(otherPosition.y);
        return Raylib.CheckCollisionRecs(internal, otherPointer);
    }

    public boolean pointWithin(Vec2 position, Vec2 point) {
        internal.x(position.x).y(position.y);

        return (position.x <= point.x && point.x <= position.x+width) 
            && (position.y <= point.y && point.y <= position.y+height);
    }

    public Optional<Vec2> checkRayHit(Vec2 position, Ray ray) { // TODO FIXME
        internal.x(position.x).y(position.y);

        Vec2 bottomLeft = new Vec2(position.x, position.y+height);
        Vec2 topRight = new Vec2(position.x+width, position.y);
        Vec2 bottomRight = new Vec2(position.x+width, position.y+height);

        Raylib.Vector2 point = new Raylib.Vector2();

        if (Raylib.CheckCollisionLines(ray.position.getPointer(), ray.endPoint.getPointer(), position.getPointer(), bottomLeft.getPointer(), point)) {
            return Optional.of(new Vec2(point));
        }

        if (Raylib.CheckCollisionLines(ray.position.getPointer(), ray.endPoint.getPointer(), position.getPointer(), topRight.getPointer(), point)) {
            return Optional.of(new Vec2(point));
        }

        if (Raylib.CheckCollisionLines(ray.position.getPointer(), ray.endPoint.getPointer(), topRight.getPointer(), bottomRight.getPointer(), point)) {
            return Optional.of(new Vec2(point));
        }

        if (Raylib.CheckCollisionLines(ray.position.getPointer(), ray.endPoint.getPointer(), bottomLeft.getPointer(), bottomRight.getPointer(), point)) {
            return Optional.of(new Vec2(point));
        }

        return Optional.empty();
    }

    public Vec2 dimensions() {
        return new Vec2(width, height);
    }

    public Vec2 getCenter(Vec2 position) { // Position is the top left corner.
        center.x = position.x + width*0.5f;
        center.y = position.y + height*0.5f;
        return center;
    }

    public Vec2 centerize(Vec2 desiredCenter) {
        return new Vec2(
            desiredCenter.x - (width*0.5f),
            desiredCenter.y - (height*0.5f)
        );
    }

    public void render(Vec2 position) {
        Raylib.DrawRectangle(position.xInt(), position.yInt(), width, height, color.getPointer());
    }

    public void renderRound(Vec2 position, float roundness, int segments) {
        var rec = raylibFromPosition(position);
        Raylib.DrawRectangleRounded(rec, roundness, segments, color.getPointer());
        rec.close();
    }

    public void renderLines(Vec2 position) {
        Raylib.DrawRectangleLines(position.xInt(), position.yInt(), width, height, color.getPointer());
    }

    public Raylib.Rectangle getPointer() {
        return internal;
    }

    protected Raylib.Rectangle raylibFromPosition(Vec2 pos) {
        return new Raylib.Rectangle().x(pos.x).y(pos.y).width(width).height(height);
    }
}
