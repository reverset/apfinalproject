package game;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.Consumer;

import game.ecs.Entity;

class SignalCallback<T> {
    public Consumer<T> callback;
    public boolean once;
    public Entity entityAssociation;

    public SignalCallback(Consumer<T> callback, boolean once, Entity entityAssociation) {
        this.callback = callback;
        this.once = once;
        this.entityAssociation = entityAssociation;
    }
}

public class Signal<T> implements Binded {
    private ArrayList<SignalCallback<T>> callbacks = new ArrayList<>();

    // This will unbind the signal to the callback if the entity is ever destroyed.
    public void listen(Consumer<T> callback, Entity entity) {
        if (entity != null) entity.bind(this);
        callbacks.add(new SignalCallback<>(callback, false, entity));
    }

    public void listen(Consumer<T> callback) {
        listen(callback, null);
    }

    public void listenOnce(Consumer<T> callback) {
        callbacks.add(new SignalCallback<>(callback, true, null));
    }

    public void unlinkAllFromEntity(Entity entity) {
        ArrayList<SignalCallback<?>> removals = new ArrayList<>();
        callbacks.iterator().forEachRemaining((callback) -> {
            if (callback.entityAssociation == entity) {
                removals.add(callback);
            }
        });

        removals.forEach(callbacks::remove);
    }

    public void emit(T value) {
        ListIterator<SignalCallback<T>> iter = callbacks.listIterator();

        while (iter.hasNext()) {
            SignalCallback<T> call = iter.next();
            call.callback.accept(value);

            if (call.once) iter.remove();
        }
    }

    @Override
    public void unbind(Entity entity) {
        unlinkAllFromEntity(entity);
    }

}
