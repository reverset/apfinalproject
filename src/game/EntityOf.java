package game;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class EntityOf<T extends ECSystem> extends Entity {
    private Class<T> systemClass;
    private T actualSystem;

    public EntityOf(String name, Class<T> system) {
        super(name);
        this.systemClass = system;
    }

    public T getMainSystem() {
        return actualSystem;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entity register(ECSystem system) {
        super.register(system);
        if (system.getClass() == systemClass) {
            actualSystem = (T) system;
        }
        return this;
    }
    
}
