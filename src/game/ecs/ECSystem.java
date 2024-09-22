package game.ecs;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.raylib.Raylib;

import game.GameLoop;
import game.ScheduledAction;

public abstract class ECSystem {
	public Entity entity = null;
	
	public abstract void setup();

	protected void schedule(List<Supplier<Boolean>> actions) {
		GameLoop.schedule(new ScheduledAction(entity, actions));
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Component> T require(Class<T> component) {
		Iterator<Component> iter = entity.componentIterator();
		
		while (iter.hasNext()) {
			Component c = iter.next();
			if (c.getClass() == component) {
				return (T) c;
			}
		}
		
		throw new RequireException(this.getClass().getName() + " requires component '" + component.getName() + "', which is not present in " + entity.name);
	}

	protected <T extends Component> T requireOrAdd(Class<T> component, Supplier<T> alt) {
		try {
			return require(component);
		} catch (RequireException e) {
			T comp = alt.get();
			entity.addComponent(comp);
			return comp;
		}
	}

	protected <T extends ECSystem> T requireOrAddSystem(Class<T> system, Supplier<T> alt) {
		try {
			return requireSystem(system);
		} catch (RequireException e) {
			T sys = alt.get();
			GameLoop.defer(() -> entity.register(sys));
			return sys;
		}
	}

	protected <T extends Component> Optional<T> optionallyRequire(Class<T> component) {
		try {
			return Optional.of(require(component));
		} catch (RequireException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends ECSystem> T requireSystem(Class<T> system) {
		Iterator<ECSystem> iter = entity.systemIterator();
		
		while (iter.hasNext()) {
			ECSystem c = iter.next();
			if (c.getClass() == system) {
				return (T) c;
			}
		}
		
		throw new RequireException(this.getClass().getName() + " requires system '" + system.getName() + "', which is not present in " + entity.name);
	}
	
	protected static float delta() {
		return Raylib.GetFrameTime();
	}

	protected static float time() {
		return (float) Raylib.GetTime();
	}

	protected static double timeDouble() {
		return Raylib.GetTime();
	}
	
	public void ready() {} // Called when an object is added to a scene. Or if it is already in one, immediately.
	public void frame() {} // Called every frame.
	public void render() {} // Also called every frame, but with the frame buffer ready and after the frame call.
	public void hudRender() {} // Called every frame to render UI. Does not follow camera.
	public void destroy() {} // Called when an Entity is removed from the game.
}
