package game.core;

import java.util.function.Consumer;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class PostMortem extends ECSystem {
    Health health;
    
    Consumer<Entity> action;

    public PostMortem(Consumer<Entity> action) {
        this.action = action;
    }

    @Override
    public void setup() {
        health = require(Health.class);
    }

    @Override
    public void ready() {
        health.onDeath.listen(n -> {
            action.accept(entity);
        }, entity);
    }
}
