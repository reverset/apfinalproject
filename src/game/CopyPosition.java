package game;

import java.util.function.Consumer;
import java.util.function.Supplier;

import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class CopyPosition extends ECSystem {

    private Transform trans;
    private Supplier<Entity> entity;
    private Transform otherTrans;

    public CopyPosition(Supplier<Entity> entity) {
        this.entity = entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }
    
    @Override
    public void frame() {
        if (otherTrans == null) return;
        trans.position.setEq(otherTrans.position.x, otherTrans.position.y);
    }

    @Override
    public void ready() {
        getOtherTransform();
    }
    
    private void getOtherTransform() {
        if (entity == null) return;
        Entity e = entity.get();
        if (e == null) return;
        otherTrans = e.getComponent(Transform.class).orElseThrow(() -> new RecoverableException("Cannot copy position of an entity that lacks a transform!"));
    }

    public void copyEntity(Supplier<Entity> entity) {
        this.entity = entity;
        getOtherTransform();
    }
}
