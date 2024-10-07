package game.core;

import com.raylib.Raylib;

import game.Color;
import game.GameLoop;
import game.RemoveAfter;
import game.Signal;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Bullet extends ECSystem {
    public static final int BULLET_DAMAGE = 5;

    public Entity owner;

    public int damage;
    public Transform trans;
    public Tangible tangible;

    RemoveAfter removeAfter;

    public Signal<Physics> onHit = new Signal<>();

    Rect rect;
    Object[] ignoreTags;

    public Bullet(Entity owner, int damage, Object[] ignoreTags) {
        this.owner = owner;
        this.damage = damage;
        this.ignoreTags = ignoreTags;
    }

    @Override
    public void setup() {
        tangible = require(Tangible.class);
        trans = require(Transform.class);
        rect = require(Rect.class);
        removeAfter = requireSystem(RemoveAfter.class);

        rect.color = rect.color.cloneIfImmutable();

        entity.addTags(GameTags.BULLET);
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void ready() {
        tangible.onCollision.listen((otherPhysics) -> {
            if (otherPhysics.entity != owner && !otherPhysics.entity.hasTag(GameTags.BULLET) && !otherPhysics.entity.hasAnyTag(ignoreTags)) {
                otherPhysics.entity.getComponent(Health.class).ifPresent((health) -> health.damage(getDamage()));
                otherPhysics.entity.getSystem(Physics.class).ifPresent((physics) -> physics.impulse(tangible.velocity.normalize().multiplyEq(100)));
                GameLoop.safeDestroy(entity);

                GameLoop.safeTrack(DamageNumber.makeEntity(trans.position, getDamage(), Color.WHITE));

                onHit.emit(otherPhysics);
            }
        }, entity);
    }

    @Override
    public void render() {
        Vec2 center = rect.getCenter(trans.position);

        long left = removeAfter.millisLeft();
        final double FADE_THRESHOLD = 1_000.0;
        double alphaCoeff = 1.0;
        if (left < FADE_THRESHOLD) {
            long dur = removeAfter.duration.toMillis();
            alphaCoeff = Math.max(left / FADE_THRESHOLD, 0);
        }

        rect.color.a = (byte) (alphaCoeff*255);
        byte temp = rect.color.a;
        rect.color.a = (byte) (64*alphaCoeff);
        Raylib.DrawLineEx(center.getPointer(), center.minus(tangible.velocity.normalize().multiplyEq(20)).getPointer(), 10f, rect.color.getPointer());
        rect.color.a = temp;
    }
}
