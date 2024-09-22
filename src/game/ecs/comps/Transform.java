package game.ecs.comps;

import game.ecs.Component;
import game.Vec2;

public class Transform implements Component {
	public Vec2 position;
	public float rotation;
	
	public Transform(Vec2 position, float rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	public Transform(Vec2 position) {
		this(position, 0);
	}
	public Transform() {
		this(new Vec2(), 0);
	}

	public Transform withPosition(Vec2 pos) {
		this.position = pos;
		return this;
	}

	public Transform clone() {
		return new Transform(position.clone(), rotation);
	}
}
