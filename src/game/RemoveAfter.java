package game;

import java.time.Duration;

import game.ecs.ECSystem;

public class RemoveAfter extends ECSystem {

    public final Duration duration; 

    private final GameTimeStopwatch timer = new GameTimeStopwatch();

    public RemoveAfter(Duration duration) {
        this.duration = duration;
    }

    public long millisLeft() {
        return timer.millisUntil(duration.toMillis());
    }

    @Override
    public void setup() {
        timer.bindTo(entity);
    }

    @Override
    public void ready() {
        timer.start();
    }


    @Override
    public void frame() {
        if (timer.hasElapsed(duration)) {
            GameLoop.safeDestroy(entity);
        }
    }
}
