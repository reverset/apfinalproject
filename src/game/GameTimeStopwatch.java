package game;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class GameTimeStopwatch extends Stopwatch implements Binded {
    private double elapsedMillis = -1;

    private ECSystem autoTickSys = null;
    private boolean ignoreUnstartedTick = false;

    public GameTimeStopwatch bindTo(Entity e) {
        e.bind(this);
        setupAutoTickSys(e);
        return this;
    }

    private ECSystem setupAutoTickSys(Entity entity) {
        var e = new ECSystem() {
            @Override
            public void setup() {}
            
            @Override
            public void frame() {
                tick(delta() * 1_000f);
            }
        };
        autoTickSys = e;
        entity.registerDeferred(autoTickSys);
        ignoreUnstartedTick = true;
        return e;
    }

    public void tick(float delta) {
        if (elapsedMillis == -1) {
            if (ignoreUnstartedTick) return;
            System.out.println("Cannot tick unstarted GameTimeStopwatch");
            throw new RecoverableException();
        }
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
        return elapsedMillis / 1_000 > seconds;
    }

    @Override
    public boolean hasStarted() {
        return elapsedMillis != -1;
    }

    @Override
    public long millisElapsed() {
        return (long) elapsedMillis;
    }

    @Override
    public void unbind(Entity e) {
        stop();
        GameLoop.defer(() -> {
            e.unregister(autoTickSys);
        });
    }
}
