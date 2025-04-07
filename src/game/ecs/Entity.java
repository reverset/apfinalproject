package game.ecs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import game.Binded;
import game.Game;
import game.GameLoop;
import game.Signal;

public class Entity {
	public final Signal<Void> onReady = new Signal<>();
	public final Signal<Void> onDestroy = new Signal<>();
	public boolean runWhilePaused = false;

	private final ArrayList<ECSystem> systems = new ArrayList<>();
	private final ArrayList<Component> components = new ArrayList<>();

	private final ArrayList<Object> tags = new ArrayList<>();

	private final ArrayList<Binded> binded = new ArrayList<>();
	
	public final String name;

	private boolean hidden = false;

	private boolean manifested = false;


	public Entity(String name) {
		this.name = name;
	}

	public Entity setPauseBehavior(boolean work) {
		runWhilePaused = work;
		return this;
	}

	public void hide() {
		hidden = true;
	}

	public void show() {
		hidden = false;
	}

	public boolean isHidden() {
		return hidden;
	}

	public Entity addTags(Object... tag) {
		for (var t : tag) {
			tags.add(t);
		}
		return this;
	}

	public boolean hasTag(Object desiredTag) {
		for (Object tag : tags) {
			if (tag.equals(desiredTag)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasTags(Object[] desiredTags) {
		if (desiredTags.length == 0) return false;

		int tags = 0;
		for (Object tag : desiredTags) {
			if (hasTag(tag)) {
				tags += 1;
			}
		}

		return tags == desiredTags.length;
	}

	public boolean hasAnyTag(Object[] desiredTags) {
		for (Object tag : desiredTags) {
			if (hasTag(tag)) {
				return true;
			}
		}
		return false;
	}

	public void bind(Binded bind) {
		if (!binded.contains(bind)) binded.add(bind);
	}

	public boolean isManifested() {
		return manifested;
	}
	
	public void ready() {
		manifested = true;
		systems.forEach(ECSystem::ready);
		onReady.emit(null);
	}
	
	public void frame() {
		systems.forEach(ECSystem::frame);
	}
	
	public void render() {
		if (hidden) return;
		systems.forEach(ECSystem::render);
	}

	public void hudRender() {
		systems.forEach(ECSystem::hudRender);
	}

	public void infrequentUpdate() {
		systems.forEach(ECSystem::infrequentUpdate);
	}

	public void destroy() {
		manifested = false;
		systems.forEach(ECSystem::destroy);
		onDestroy.emit(null);
		binded.forEach((bind) -> bind.unbind(this));
		binded.clear();
	}
	
	public Iterator<Component> componentIterator() {
		return components.iterator();
	}

	public Iterator<ECSystem> systemIterator() {
		return systems.iterator();
	}

	public Entity registerDeferred(ECSystem system) {
		GameLoop.defer(() -> register(system));
		return this;
	}

	public Entity register(ECSystem system) {
		Objects.requireNonNull(system);
		system.entity = this;
		systems.add(system);
		system.setup();
		return this;
	}

	public Entity unregister(ECSystem system) {
		Objects.requireNonNull(system);
		if (systems.contains(system)) {
			systems.remove(system);
		} else {
			// todo
		}
		return this;
	}
	
	public Entity addComponent(Component component) {
		Objects.requireNonNull(component);
		components.add(component);
		return this;
	}

	public Entity addComponent(Supplier<Component> comSupplier) {
		Objects.requireNonNull(comSupplier);
		addComponent(comSupplier.get());
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends ECSystem> Optional<T> getSystem(Class<T> sys) {
		Objects.requireNonNull(sys);
		Iterator<ECSystem> iter = systemIterator();
		
		while (iter.hasNext()) {
			ECSystem c = iter.next();
			if (sys.isAssignableFrom(c.getClass())) {
				return Optional.of((T) c);
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T extends Component> Optional<T> getComponent(Class<T> sys) {
		Objects.requireNonNull(sys);
		Iterator<Component> iter = componentIterator();
		
		while (iter.hasNext()) {
			Component c = iter.next();
			if (c.getClass() == sys) {
				return Optional.of((T) c);
			}
		}
		return Optional.empty();
	}
}
