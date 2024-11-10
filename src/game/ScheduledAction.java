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
        if (entity != null) entity.bind(this);
        this.actions = new LinkedList<>(actions);
    }

    @SuppressWarnings("unchecked") // Java, why? Why can't a list clone itself and return the right type instead of Object?
    public ScheduledAction clone() {
        return new ScheduledAction(entity, (LinkedList<Supplier<Boolean>>) actions.clone());
    }

    public boolean update() { // returning true means that the action has finished.
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
