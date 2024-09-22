package game;

import java.time.Duration;

import game.ecs.ECSystem;

public class RemoveAfter extends ECSystem {

    private Duration duration; 
    private long startTimeMillis = 0;

    public RemoveAfter(Duration duration) {
        this.duration = duration;
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
