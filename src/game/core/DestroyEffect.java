package game.core;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RemoveAfter;
import game.Tween;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import java.time.Duration;

import com.raylib.Raylib;

public class DestroyEffect extends ECSystem {
    public static EntityOf<DestroyEffect> makeEntity(Vec2 dimensions, Vec2 position) {
        EntityOf<DestroyEffect> entity = new EntityOf<>("Destroy Effect", DestroyEffect.class);
        
        entity
            .addComponent(new Transform(position))
            .register(new DestroyEffect(dimensions));

        return entity;
    }

    private Vec2 dimensions;
    private Transform trans;

    private Color desiredColor = new Color(255, 255, 255, 255);

    private Vec2 firstHalfPosition;
    private Vec2 secondHalfPosition;

    private Vec2 firstHalfVelocity;
    private Vec2 secondHalfVelocity;

    public DestroyEffect(Vec2 dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        firstHalfPosition = trans.position;
        secondHalfPosition = trans.position.add(dimensions.x*0.5f, 0);

        firstHalfVelocity = Vec2.randomUnit().multiply(50);
        firstHalfVelocity.x = -Math.abs(firstHalfVelocity.x);
        secondHalfVelocity = firstHalfVelocity.negate();
    }

    @Override
    public void ready() {
        GameLoop.defer(() -> {
            var tween = new Tween<>(Tween.lerp(255, 0), 0.5, val -> {
                desiredColor.a = val.byteValue();
            });
            entity.register(tween);
            tween.start();
        });
    }

    @Override
    public void frame() {
        firstHalfPosition.addEq(firstHalfVelocity.multiply(delta()));
        secondHalfPosition.addEq(secondHalfVelocity.multiply(delta()));
    }

    @Override
    public void render() {
        Raylib.DrawRectangle(firstHalfPosition.xInt(), firstHalfPosition.yInt(), dimensions.xInt()/2, dimensions.yInt(), desiredColor.getPointer());
        Raylib.DrawRectangle(secondHalfPosition.xInt(), secondHalfPosition.yInt(), dimensions.xInt()/2, dimensions.yInt(), desiredColor.getPointer());
    }
    
}
