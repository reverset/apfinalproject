package game.core;

import java.util.ArrayList;
import java.util.function.Consumer;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class PostMortem extends ECSystem {
    Health health;
    
    private final ArrayList<Consumer<Entity>> actions = new ArrayList<>();

    public PostMortem(Consumer<Entity> action) {
        actions.add(action);
    }

    @Override
    public void setup() {
        health = require(Health.class);
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            for (var act : actions) {
                act.accept(entity);
            }
        }, entity);
    }

    public PostMortem addWill(Consumer<Entity> action) {
        actions.add(action);
        return this;
    }
}
