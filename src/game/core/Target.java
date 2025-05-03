package game.core;

import java.util.Optional;

import game.RecoverableException;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public record Target(Entity entity, Transform trans, Optional<Health> health, Optional<Physics> physics) {
    public static Target ofEntity(Entity entity) {
        final var trans = entity.getComponent(Transform.class).orElseThrow(() -> new RecoverableException("Cannot make a target of an entity lacking a Transform!"));
        final var health = entity.getComponent(Health.class);
        final var physics = entity.getSystem(Physics.class);
        
        return new Target(entity, trans, health, physics);
    }
}
