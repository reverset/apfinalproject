package game.core;

import java.util.Optional;

import game.Vec2;

public class Ray {
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

        endPoint = position.add(direction.multiplyEq(length));
    }

    public Optional<Vec2> test() {
        return Physics.testRay(this);
    }
}
