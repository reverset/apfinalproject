package game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class Physics extends ECSystem {
    public enum Kind {
        DYNAMIC,
        STATIC,
    }
    public static final boolean DEBUG = true;
    public Vec2 hitBoxOffset;

    private static final ArrayList<ArrayList<Physics>> physicsObjects = new ArrayList<>();

    static {
        physicsObjects.add(new ArrayList<>(100));
        physicsObjects.add(new ArrayList<>(100));
        physicsObjects.add(new ArrayList<>(100));
    }

    private Rect collisionRect;
    private Transform trans;
    private Tangible tangible;
    private Kind kind;

    private int layer;
    private int layerMask;

    private float impulseResistance = 0;
    
    public Physics(Kind kind, int layer, int layerMask, Vec2 hitBoxOffset) {
        this.kind = kind;
        this.layer = layer;
        this.layerMask = layerMask;
        this.hitBoxOffset = hitBoxOffset;
        physicsObjects.get(layer).add(this);
    }

    public Physics(Kind kind, int layer, int layerMask) {
        this(kind, layer, layerMask, Vec2.zero());
    }

    public Physics(int layer, int layerMask) {
        this(Kind.DYNAMIC, layer, layerMask);    
    }

    public Physics(int layer, int layerMask, Vec2 hitBoxOffset) {
        this(Kind.DYNAMIC, layer, layerMask, hitBoxOffset);    
    }

    public void setHitboxOffset(Vec2 offset) {
        hitBoxOffset = offset;
    }

    public static List<Physics> testCircle(Vec2 pos, float radius, int layerMask) {
        ArrayList<Physics> res = new ArrayList<>();
        for (var obj : physicsObjects.get(layerMask)) {
            for (final var corner : obj.collisionRect.getCorners(obj.trans.position)) {
                if (pos.distance(corner) <= radius) {
                    res.add(obj);
                    break;
                }
            }
        }
        return Collections.unmodifiableList(res);
    }

    public static Optional<Ray.RayResult> testRay(Ray ray) {
        for (var obj : physicsObjects.get(ray.layerMask)) {
            Optional<Vec2> p = obj.collisionRect.checkRayHit(obj.trans.position.add(obj.hitBoxOffset), ray);
            if (p.isPresent()) {
                return Optional.of(new Ray.RayResult(p.get(), obj));
            }
        }
        return Optional.empty();
    }

    public static List<Ray.RayResult> testRayForAll(Ray ray) {
        List<Ray.RayResult> results = new ArrayList<>();
        for (var obj : physicsObjects.get(ray.layerMask)) {
            Optional<Vec2> p = obj.collisionRect.checkRayHit(obj.trans.position.add(obj.hitBoxOffset), ray);
            if (p.isPresent()) {
                results.add(new Ray.RayResult(p.get(), obj));
            }
        }
        return results;
    }

    public void unregister() {
        for (var list : physicsObjects) {
            if (list.contains(this)) {
                list.remove(this);
                return;
            }
        }
    }

    @Override
    public void setup() {
        collisionRect = require(Rect.class);
        trans = require(Transform.class);
        tangible = require(Tangible.class);
    }

    @Override
    public void frame() {
        if (kind == Kind.STATIC) return;

        trans.position.addEq(tangible.velocity.x * delta(), tangible.velocity.y * delta());

        // checkCollisions();
    }

    @Override
    public void infrequentUpdate() {
        checkCollisions();
    }

    private void checkCollisions() { // still a performance bug
        for (var obj : physicsObjects.get(layerMask)) {
            if (obj == this) continue;

            if (obj.collisionRect.overlaps(
                obj.trans.position.x+obj.hitBoxOffset.x, obj.trans.position.y+obj.hitBoxOffset.y, 
                trans.position.x + hitBoxOffset.x, trans.position.y + hitBoxOffset.y, 
                collisionRect)) {
                
                tangible.onCollision.emit(obj);
            }
        }
    }

    public void setImpulseResistance(float resistance) {
        impulseResistance = resistance;
    }

    public void impulse(Vec2 force) {
        tangible.velocity.addEq(force.moveTowards(Vec2.ZERO, impulseResistance));
    }

    public void applyForce(Vec2 force) {
        tangible.velocity.addEq(force.multiply(delta()));
    }

    public Tangible getTangible() {
        return tangible;
    }

    @Override
    public void destroy() {
        unregister();
    }
    
    @Override
    public void render() {
        if (DEBUG) collisionRect.renderLines(trans.position.add(hitBoxOffset));
    }
}
