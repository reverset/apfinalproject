package game.core;

import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class BossBody extends ECSystem {

    Transform trans;
    Supplier<Vec2> posSupplier;
    Supplier<Vec2> velSupplier;

    public BossBody(Supplier<Vec2> posSupplier, Supplier<Vec2> velSupplier) {
        this.posSupplier = posSupplier;
        this.velSupplier = velSupplier;
    }

    public static EntityOf<BossBody> makeEntity(Supplier<Vec2> posSupplier, Supplier<Vec2> velSupplier) {
        EntityOf<BossBody> entity = new EntityOf<>("boss body", BossBody.class);

        entity
            .addComponent(new Poly(BossEnemy.SIDES, BossEnemy.RADIUS, Color.DARK_RED))
            .addComponent(new Transform())
            .register(new PolyRenderer())
            .register(new BossBody(posSupplier, velSupplier));
        
        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public void frame() {
        Vec2 desiredPos = posSupplier.get().minus(velSupplier.get());
        float coeff = trans.position.distance(desiredPos)/8;
        trans.position.lerpEq(desiredPos, Math.min(coeff*delta(), 1));
    }

    @Override
    public void render() {
        
    }
    
}
