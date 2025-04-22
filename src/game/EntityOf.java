package game;


import java.util.Iterator;

import game.ecs.Component;
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
    

    public <K extends ECSystem> EntityOf<K> into(Class<K> clazz) {
        if (!clazz.isAssignableFrom(systemClass)) {
            throw new RecoverableException("Failed to convert EntityOf<" + systemClass + "> to EntityOf<" + clazz + ">");
        }
        EntityOf<K> entity = new EntityOf<>(name, clazz);

        Iterator<ECSystem> sys = systemIterator();
        Iterator<Component> comps = componentIterator();

        while (comps.hasNext()) {
            var c = comps.next();
            entity.addComponent(c);
        }

        while (sys.hasNext()) {
            var s = sys.next();
            entity.register(s);
        }

        return entity;
    }

    @Override
    public String toString() {
        return "EntityOf<" + systemClass.getName() + ">";
    }
}
