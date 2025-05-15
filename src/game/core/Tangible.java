package game.core;

import game.Signal;
import game.Vec2;
import game.ecs.Component;

public class Tangible implements Component {
    public Vec2 velocity = new Vec2();
    
    private boolean isTouchable = true;

    public Tangible() {
        
    }

    public boolean isTangible() {
        return isTouchable;
    }

    public void setTangible(boolean t) {
        isTouchable = t;
    }

    public Tangible(Vec2 velocity) {
        this.velocity = velocity;
    }

    public void impulse(Vec2 speed) {
        velocity.addEq(speed);
    }

    public Signal<Physics> onCollision = new Signal<>();
}
