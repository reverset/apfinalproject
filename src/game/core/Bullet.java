package game.core;

import java.util.Optional;

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

    protected Rect rect;
    protected Object[] ignoreTags;

    protected Optional<Effect> effect;

    private Weapon2 weapon;

    public Bullet(Entity owner, int damage, Optional<Effect> effect, Object[] ignoreTags, Weapon2 weapon) {
        this.owner = owner;
        this.damage = damage;
        this.ignoreTags = ignoreTags;
        this.effect = effect;
        this.weapon = weapon;
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

    public DamageInfo computeDamage(Entity victim) {
        DamageInfo info = new DamageInfo(damage, victim, null, trans.position.clone()).setColor(weapon.getHitMakerColor()).setAttacker(owner);
        return effect.isPresent()
            ? effect.get().computeDamage(info)
            : info;
    }

    @Override
    public void ready() {
        tangible.onCollision.listen((otherPhysics) -> {
            if (otherPhysics.entity != owner && !otherPhysics.entity.hasTag(GameTags.BULLET) && !otherPhysics.entity.hasAnyTag(ignoreTags)) {
                DamageInfo damage = computeDamage(otherPhysics.entity);
                
                var healthOpt = otherPhysics.entity.getComponent(Health.class);
                if (healthOpt.isPresent()) damage = healthOpt.get().damageOrHeal(damage);

                otherPhysics.entity.getSystem(Physics.class).ifPresent((physics) -> physics.impulse(tangible.velocity.normalize().multiplyEq(100)));
                GameLoop.safeDestroy(entity);

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
        Raylib.DrawLineEx(center.asCanonicalVector2(), center.minusEq(tangible.velocity.normalize().multiplyEq(20)).allocateRaylibVector2(), 10f, rect.color.getPointer());
        rect.color.a = temp;
    }
}
