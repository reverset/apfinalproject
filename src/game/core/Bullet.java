package game.core;

import game.Color;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;
import com.raylib.Raylib;


public class Bullet extends ECSystem {
    public static final int BULLET_DAMAGE = 5;

    public Entity owner;

    public Transform trans;
    public Tangible tangible;
    private Rect rect;
    private Object[] ignoreTags;

    public Bullet(Entity owner, Object[] ignoreTags) {
        this.owner = owner;
        this.ignoreTags = ignoreTags;
    }

    @Override
    public void setup() {
        tangible = require(Tangible.class);
        trans = require(Transform.class);
        rect = require(Rect.class);

        entity.addTags(GameTags.BULLET);
    }

    @Override
    public void ready() {
        tangible.onCollision.listen((otherPhysics) -> {
            if (otherPhysics.entity != owner && !otherPhysics.entity.hasTag(GameTags.BULLET) && !otherPhysics.entity.hasTags(ignoreTags)) {
                otherPhysics.entity.getComponent(Health.class).ifPresent((health) -> health.damage(BULLET_DAMAGE));
                otherPhysics.entity.getSystem(Physics.class).ifPresent((physics) -> physics.impulse(tangible.velocity.normalize().multiply(100)));
                GameLoop.safeDestroy(entity);
            }
        }, entity);
    }

    @Override
    public void render() {
        Vec2 center = rect.getCenter(trans.position);
        Color col = rect.color.clone();
        col.a = (byte) 64;
        Raylib.DrawLineEx(center.getPointer(), center.minus(tangible.velocity.normalize().multiply(20)).getPointer(), 10f, col.getPointer());
    }
}
