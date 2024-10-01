package game.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import game.Color;
import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public class RayWeapon extends Weapon {

    Ray ray;

    Vec2 position;
    Vec2 direction;

    Object[] ignoreTags;

    float distance;
    float force = 0;

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
    
}
