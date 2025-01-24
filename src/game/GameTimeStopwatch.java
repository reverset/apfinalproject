package game;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class GameTimeStopwatch extends Stopwatch implements Binded {
    private double elapsedMillis = -1;

    private ECSystem autoTickSys = null;

    public void bindTo(Entity e) {
        e.bind(this);
        setupAutoTickSys();
    }

    private ECSystem setupAutoTickSys() {
        var e = new ECSystem() {
            @Override
            public void setup() {}
        };
        autoTickSys = e;
        return e;
    }

    public void tick(float delta) {
        elapsedMillis += delta;
    }

    @Override
    public void start() {
        if (elapsedMillis == -1) elapsedMillis = 0;
    }

    @Override
    public void restart() {
        elapsedMillis = 0;
    }

    @Override
    public void stop() {
        elapsedMillis = -1;
    }

    @Override
    public boolean hasElapsedSeconds(double seconds) {
        return elapsedMillis / 1_000 < seconds;
    }

    @Override
    public boolean hasStarted() {
        return elapsedMillis != -1;
    }

    @Override
    public void unbind(Entity e) {
        stop();
        e.unregister(autoTickSys);
    }
}
