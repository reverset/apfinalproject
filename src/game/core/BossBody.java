package game.core;

import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class BossBody extends ECSystem {

    BossEnemy boss;

    Transform trans;
    Supplier<Vec2> posSupplier;
    Supplier<Vec2> velSupplier;

    Tangible tangible;
    Health health;

    public BossBody(BossEnemy boss, Supplier<Vec2> posSupplier, Supplier<Vec2> velSupplier) {
        this.boss = boss;
        this.posSupplier = posSupplier;
        this.velSupplier = velSupplier;
    }

    public static EntityOf<BossBody> makeEntity(BossEnemy boss, Supplier<Vec2> posSupplier, Supplier<Vec2> velSupplier) {
        EntityOf<BossBody> entity = new EntityOf<>("boss body", BossBody.class);

        entity
            .addComponent(new Poly(BossEnemy.SIDES, BossEnemy.RADIUS, Color.DARK_RED))
            .addComponent(new Transform())
            .addComponent(Rect.around(BossEnemy.RADIUS*2, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Health(Integer.MAX_VALUE))
            .register(new Physics(0, 0, new Vec2(-BossEnemy.RADIUS, -BossEnemy.RADIUS)))
            .register(new PolyRenderer())
            .register(new BossBody(boss, posSupplier, velSupplier))
            .addTags(new Object[]{GameTags.ENEMY_TEAM});
        
        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);

        health.onDamage.listen(dmg -> {
            boss.health.damage(dmg);
        }, entity);
    }

    @Override
    public void frame() {
        tangible.velocity.setEq(0, 0);
        Vec2 desiredPos = posSupplier.get().minus(velSupplier.get());
        float coeff = trans.position.distance(desiredPos)/8;
        trans.position.lerpEq(desiredPos, Math.min(coeff*delta(), 1));
    }

    @Override
    public void render() {
        
    }
    
}
