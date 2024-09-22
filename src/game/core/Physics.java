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
    private static final ArrayList<Physics> physicsObjects = new ArrayList<>();

    private Rect collisionRect;
    private Transform trans;
    private Tangible tangible;
    private Kind kind;

    
    public Physics(Kind kind) {
        this.kind = kind;
        physicsObjects.add(this);
    } 

    public Physics() {
        this(Kind.DYNAMIC);    
    }

    public void unregister() {
        physicsObjects.remove(this);
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

        trans.position.addEq(tangible.velocity.multiply(delta()));

        checkCollisions();
    }

    private void checkCollisions() {
        for (var obj : physicsObjects) {
            if (obj == this) continue;

            if (obj.collisionRect.overlaps(obj.trans.position, trans.position, collisionRect)) {
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
    
}
