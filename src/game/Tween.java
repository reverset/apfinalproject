package game;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.raylib.Raylib;

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

    private DoubleSupplier timeSupp;

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

    public static TweenFunction<String> reveal(String desiredMessage) {
        int len = desiredMessage.length();
        return percent -> {
            int revealed = (int) (percent * len);
            return desiredMessage.substring(0, revealed);
        };
    }

    // This feels wrong but idk
    public static TweenFunction<Float> overEase(float from, float to, float overshoot) {
        return percent -> {
            final float onePlusOver = overshoot + 1;

            float calc = 1 + onePlusOver * ((float) Math.pow(percent - 1, 3)) + overshoot * ((float) Math.pow(percent - 1, 2));
            return (to - from) * calc + from;
        };
    }

    public static TweenFunction<Vec2> circleLerp(float fromAngle, float toAngle, float distance, Supplier<Vec2> center) {
        return percent -> {
            var desiredAngle = MoreMath.lerpAngle(fromAngle, toAngle, (float) percent);
            return Vec2.fromAngle(desiredAngle).multiplyEq(distance).addEq(center.get());
        };
    }


    public Tween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater) {
        this(supplier, durationSeconds, true, updater);
    }

    public Tween(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater, DoubleSupplier timeSupp) {
        this(supplier, durationSeconds, true, updater, timeSupp);
    }

    public Tween(TweenFunction<T> supplier, double durationSeconds, boolean shouldDestroy, Consumer<T> updater) {
        this(supplier, durationSeconds, shouldDestroy, updater, Raylib::GetTime);
    }

    public Tween(TweenFunction<T> supplier, double durationSeconds, boolean shouldDestroy, Consumer<T> updater, DoubleSupplier timeSupp) {
        this.supplier = supplier;
        this.durationSeconds = durationSeconds;
        this.updater = updater;
        this.shouldDestroy = shouldDestroy;
        this.timeSupp = timeSupp;
    }

    public Tween<T> setDestroy(boolean destroy) {
        shouldDestroy = destroy;
        return this;
    }

    public Tween<T> start() {
        startTime = timeSupp.getAsDouble();
        return this;
    }

    public Tween<T> runWhilePaused(boolean run) {
        entity.runWhilePaused = run;
        return this;
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
        return makeEntity(supplier, durationSeconds, updater, Raylib::GetTime);
    }

    public static <T> Entity makeEntity(TweenFunction<T> supplier, double durationSeconds, Consumer<T> updater, DoubleSupplier timeSupp) {
        return new Entity("tween")
            .register(new Tween<T>(supplier, durationSeconds, updater, timeSupp));
    }

    public Tween<T> reversed() {
        return new Tween<>(percent -> supplier.supply(1 - percent), durationSeconds, shouldDestroy, updater, timeSupp);
    }

    public Tween<T> reversedEntity() {
        Tween<T> tween = reversed();
        Entity entity = new Entity("tween")
            .register(tween);
        GameLoop.safeTrack(entity);

        return tween;
    }

    @Override
    public void setup() {
        
    }

    @Override
    public void frame() {
        if (startTime == -1 || startTime == -2) return;

        double elapsed = timeSupp.getAsDouble() - startTime;
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
