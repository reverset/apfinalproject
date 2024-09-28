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
        if (actualSystem == null) {
            actualSystem = getSystem(systemClass).orElseThrow();
        }
        return actualSystem;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entity register(ECSystem system) {
        super.register(system);
        if (systemClass.isAssignableFrom(system.getClass())) {
            actualSystem = (T) system;
        }
        return this;
    }
    
}
