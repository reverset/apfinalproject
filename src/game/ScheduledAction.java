package game;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import game.ecs.Entity;

public class ScheduledAction implements Binded {
    private LinkedList<Supplier<Boolean>> actions;
    private boolean unbinded = false;
    private Entity entity;
    
    public ScheduledAction(Entity entity, List<Supplier<Boolean>> actions) {
        entity.bind(this);
        this.actions = new LinkedList<>(actions);
    }

    public ScheduledAction clone() {
        return new ScheduledAction(entity, actions);
    }

    public boolean update() {
        if (unbinded) return true;
        ListIterator<Supplier<Boolean>> iter = actions.listIterator();

        if (iter.hasNext()) {
            var act = iter.next();

            if (act.get()) {
                iter.remove();
            }
        }

        return actions.size() <= 0;
    }

    @Override
    public void unbind(Entity entity) {
        unbinded = true;
    }
}
