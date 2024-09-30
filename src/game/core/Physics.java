package game.core;

import java.util.ArrayList;

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

    private void checkCollisions() {
        for (var obj : physicsObjects.get(layerMask)) {
            if (obj == this) continue;

            if (obj.collisionRect.overlaps(obj.trans.position.add(obj.hitBoxOffset), trans.position.add(hitBoxOffset), collisionRect)) {
                tangible.onCollision.emit(obj);
            }
        }
    }

    public void impulse(Vec2 force) {
        tangible.velocity.addEq(force);
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
