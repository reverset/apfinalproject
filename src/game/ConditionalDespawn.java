package game;

import java.util.function.BooleanSupplier;

import game.ecs.ECSystem;

public class ConditionalDespawn extends ECSystem {

    private final BooleanSupplier supp;

    public ConditionalDespawn(BooleanSupplier supp) {
        this.supp = supp;

    }

    @Override
    public void setup() {
    }

    @Override
    public void infrequentUpdate() {
        if (supp.getAsBoolean()) GameLoop.safeDestroy(entity);
    }
    
}
