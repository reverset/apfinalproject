package game.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import game.Color;
import game.GameLoop;
import game.Tween;
import game.Vec2;
import game.ecs.Entity;

public class LaserWeapon extends Weapon2 {
    Vec2 position;
    Vec2 direction;

    Optional<Effect> effect;

    Color color;

    float length;
    float laserThickness;
    float knockback;

    int layerMask;
    int damage;

    Object[] ignoreTags;

    private Ray ray;
    private boolean charging = false;
    private float chargeUpSeconds;

    public LaserWeapon(int damage, Vec2 position, Vec2 direction, float chargeUpSeconds, Color color, float length, float knockback, float laserThickness, int layerMask, Object[] ignoreTags, float cooldown, Optional<Effect> effect) {
        super(cooldown, effect);
        this.position = position;
        this.direction = direction;
        this.chargeUpSeconds = chargeUpSeconds;
        this.color = color.cloneIfImmutable();
        this.color.a = 0;
        this.damage = damage;
        this.length = length;
        this.knockback = knockback;
        this.laserThickness = laserThickness;
        this.layerMask = layerMask;
        this.ignoreTags = ignoreTags;
        this.effect = effect;

        ray = new Ray(position, direction, length, layerMask);
    }

    public LaserWeapon(int damage, Vec2 position, Vec2 direction, Color color, float length, float knockback, float laserThickness, int layerMask, Object[] ignoreTags, float cooldown, Optional<Effect> effect) {
        this(damage, position, direction, 3, color, length, knockback, laserThickness, layerMask, ignoreTags, cooldown, effect);
    }

    public boolean isCharging() {
        return charging;
    }

    @Override
    public boolean canFire() {
        return super.canFire() && !isCharging();
    }

    @Override
    void forceFire(Vec2 position, Vec2 direction, Entity owner) {
        ray.updateRay(position, direction.clone());
        List<Ray.RayResult> collisions = ray.testForAll();

        for (var collision : collisions) {
            Entity collisionEntity = collision.physics().entity;
            if (collisionEntity.hasAnyTag(ignoreTags)) continue;

            Optional<Health> health = collisionEntity.getComponent(Health.class);
            health.ifPresent(h -> {
                DamageInfo info = new DamageInfo(damage, collisionEntity, this, collision.position());
                if (effect.isPresent()) {
                    info = effect.get().computeDamage(info);
                }
                info = h.damageOrHeal(info);
                // GameLoop.safeTrack(DamageNumber.makeEntity(collision.position(), info.damage(), Color.WHITE));
            });
            collision.physics().impulse(direction.multiply(knockback));
        }
    }

    public Tween<Float> chargeUp(Supplier<Vec2> position, Supplier<Vec2> direction, Entity entity, Consumer<Boolean> impending) {
        charging = true;
        var tween = GameLoop.makeTween(Tween.lerp(0, 50), chargeUpSeconds, val -> { // CLEANUP WITH TWEEN ANIMATION TODO
            ray.position = position.get();
            ray.direction = direction.get();
            ray.updateRay();
            
            color.a = val.byteValue();

            if (color.a > 40) impending.accept(true);
        }).start();

        tween.onFinish.listen(nn -> {
            fire(position.get(), direction.get(), entity);

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

    @Override
    public void render() {
        ray.renderEx(laserThickness, color);
    }
}
