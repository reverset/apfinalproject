package game.core;

import com.raylib.Raylib;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RecoverableException;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class Border extends ECSystem {

    private Transform trans;
    private float radius;
    private Player player;

    private static Border instance = null;

    public static EntityOf<Border> makeEntity(Vec2 center, float radius) {
        EntityOf<Border> e = new EntityOf<>("border", Border.class);

        e
            .addComponent(new Transform(center))
            .register(new Border(radius));

        return e;
    }

    public Border(float radius) {
        this.radius = radius;

        instance = this;
    }

    public static Border getInstance() {
        return instance;
    }

    public Vec2 getCenter() {
        return trans.position;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        player = GameLoop.findEntityByTag(GameTags.PLAYER)
            .flatMap(p -> p.getSystem(Player.class))
            .orElse(null);
    }

    @Override
    public void infrequentUpdate() {
        if (player == null) return;
        
        Vec2 diff = trans.position.minus(player.getTransform().position);
        if (diff.magnitude() > radius) {
            player.getTangible().impulse(diff.normalizeEq().multiplyEq(2_000));
        }
    }

    @Override
    public void render() {
        Raylib.DrawCircleLinesV(trans.position.asCanonicalVector2(), radius, Color.RED.getPointerNoUpdate());
        // Raylib.DrawCircle(trans.position.xInt(), trans.position.yInt(), radius, Color.RED.getPointerNoUpdate());
    }
    
}
