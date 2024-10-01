package game.core;

import java.util.List;
import java.util.Optional;

import game.Color;
import game.Vec2;

import com.raylib.Raylib;

public class Ray {
    public static record RayResult(Vec2 position, Physics physics) {}

    public Vec2 position;
    public Vec2 endPoint;
    Vec2 direction;

    float length;
    int layerMask;
    
    public Ray(Vec2 position, Vec2 direction, float length, int layerMask) {
        this.position = position;
        this.direction = direction;
        this.length = length;
        this.layerMask = layerMask;

        updateRay();
    }
    
    public void updateRay() {
        endPoint = position.add(direction.multiplyEq(length));
    }

    public Optional<Ray.RayResult> test() {
        return Physics.testRay(this);
    }

    public List<Ray.RayResult> testForAll() {
        return Physics.testRayForAll(this);
    }

    public void render(Color color) {
        Raylib.DrawLine(position.xInt(), position.yInt(), endPoint.xInt(), endPoint.yInt(), color.getPointer());
    }
}
