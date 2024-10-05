package game;

import java.time.Duration;

import game.ecs.ECSystem;

public class RemoveAfter extends ECSystem {

    public final Duration duration; 
    private long startTimeMillis = 0;

    public RemoveAfter(Duration duration) {
        this.duration = duration;
    }

    public long millisLeft() {
        return (duration.toMillis() + startTimeMillis) - System.currentTimeMillis();
    }

    @Override
    public void setup() {
        startTimeMillis = System.currentTimeMillis();
    }


    @Override
    public void frame() {
        if (System.currentTimeMillis() > duration.toMillis() + startTimeMillis) {
            GameLoop.safeDestroy(entity);
        }
    }
}
