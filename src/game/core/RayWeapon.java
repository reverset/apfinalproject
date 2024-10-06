package game.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import game.Color;
import game.GameLoop;
import game.Tween;
import game.Vec2;
import game.ecs.Entity;

public class RayWeapon extends Weapon {

    Ray ray;

    Vec2 position;
    Vec2 direction;

    Object[] ignoreTags;

    float distance;
    float force = 0;

    boolean charging = false;

    public RayWeapon(Vec2 position, Vec2 direction, int damage, float distance, float cooldown, int layerMask, Object[] ignoreTags) {
        super(null, cooldown, 0);
        this.distance = distance;
        this.position = position;
        this.direction = direction;
        this.ignoreTags = ignoreTags;
        this.damage = damage;

        ray = new Ray(position, direction, distance, layerMask);
    }

    public RayWeapon setForce(float f) {
        force = f;
        return this;
    }
    
    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void forceFire(Vec2 position, Vec2 direction) {
        ray.position = position;
        ray.direction = direction.clone();
        ray.updateRay();

        List<Ray.RayResult> collisions = ray.testForAll();

        for (var collision : collisions) {
            Entity collisionEntity = collision.physics().entity;
            if (collisionEntity.hasAnyTag(ignoreTags)) continue;

            Optional<Health> health = collisionEntity.getComponent(Health.class);
            health.ifPresent(h -> {
                h.damage(damage);
                GameLoop.safeTrack(DamageNumber.makeEntity(collision.position(), getDamage(), Color.WHITE));
            });
            collision.physics().impulse(direction.multiply(force));
        }
    }

    public boolean isCharging() {
        return charging;
    }

    public Tween<Float> chargeUp(Supplier<Vec2> position, Supplier<Vec2> direction, Color color, Entity entity, Consumer<Boolean> impending) {
        charging = true;
        var tween = GameLoop.makeTween(Tween.lerp(0, 50), 3, val -> {
            ray.position = position.get();
            ray.direction = direction.get();
            ray.updateRay();
            
            color.a = val.byteValue();

            if (color.a > 40) impending.accept(true);
        }).start();

        tween.onFinish.listen(nn -> {
            fire(position.get(), direction.get());

            GameLoop.makeTween(Tween.lerp(255, 0), 0.5, val -> {
                ray.position = position.get();
                ray.direction = direction.get();
                ray.updateRay();

                color.a = val.byteValue();

                if (color.a == 0) impending.accept(false);
                charging = false;
            }).start();

        }, entity);

        return tween;
    }
    
}
