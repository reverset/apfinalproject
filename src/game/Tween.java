package game;

import java.util.function.Consumer;

import game.ecs.ECSystem;
import game.ecs.Entity;

public class Tween<T> extends ECSystem {
    @FunctionalInterface
    public static interface TweenFunction<T> {
        T supply(double normalPercentage);
    }

    public final Signal<Void> onFinish = new Signal<>();

    private double startTime = -1;
    private Consumer<T> updater;
    private TweenFunction<T> supplier;
    private double durationSeconds;
    private boolean shouldDestroy;

    public static TweenFunction<Double> lerp(double from, double to) {
        return percent -> MoreMath.lerp(from, to, percent);
    }

    public static TweenFunction<Float> lerp(float from, float to) {
        return percent -> MoreMath.lerp(from, to, (float) percent);
    }

    public static TweenFunction<Vec2> lerp(Vec2 from, Vec2 to) {
        return percent -> from.lerp(to, (float) percent);
    }

    public static TweenFunction<float[]> lerp(float[] from, float[] to) {
        return percent -> MoreMath.lerp(from, to, (float) percent);
    }


    public Tween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
        this(supplier, durationSeconds, true, updater);
    }

    public Tween(TweenFunction<T> supplier, double durationSeconds, boolean shouldDestroy, Consumer<T> updater) {
        this.supplier = supplier;
        this.durationSeconds = durationSeconds;
        this.updater = updater;
        this.shouldDestroy = shouldDestroy;
    }

    public Tween<T> setDestroy(boolean destroy) {
        shouldDestroy = destroy;
        return this;
    }

    public void start() {
        startTime = timeDouble();
    }

    public void stop() {
        startTime = -1;
    }

    public void stopAndDestroy() {
        startTime = -1;
        if (shouldDestroy) GameLoop.safeDestroy(entity);
    }

    public boolean isFinished() {
        return startTime == -2;
    }

    public boolean isRunning() {
        return startTime >= 0;
    }

    public void reset() {
        startTime = -1;
    }

    public static <T> Tween<T> makeAndSchedule(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
        Tween<T> tween = new Tween<>(supplier, durationSeconds, updater);
        GameLoop.safeTrack(new Entity("tween").register(tween));
        tween.start();
        return tween;
    }

    public static <T> Entity makeEntity(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
        return new Entity("tween")
            .register(new Tween<T>(supplier, durationSeconds, updater));
    }

    @Override
    public void setup() {
        
    }

    @Override
    public void frame() {
        if (startTime == -1 || startTime == -2) return;

        double elapsed = timeDouble() - startTime;
        double percent = Math.min(elapsed / durationSeconds, 1.0);

        updater.accept( supplier.supply(percent) );

        if (percent >= 1.0) {
            startTime = -2;
            onFinish.emit(null);
            stopAndDestroy();
            startTime = -2;
        }
    }
    
}
