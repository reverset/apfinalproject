package game.core;

import game.Signal;
import game.Vec2;
import game.ecs.Component;

public class Tangible implements Component {
    public Vec2 velocity = new Vec2();

    public Tangible() {
        
    }

    public Tangible(Vec2 velocity) {
        this.velocity = velocity;
    }

    public Signal<Physics> onCollision = new Signal<>();
}
