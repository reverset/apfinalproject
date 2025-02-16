package game;

import java.time.Duration;
import java.util.function.DoubleSupplier;

import com.raylib.Raylib;


public class Stopwatch {
    double startTime = -1;
    DoubleSupplier timeSupp;

    public Stopwatch(DoubleSupplier timeSupp) {
        this.timeSupp = timeSupp;
    }

    public static Stopwatch ofRealTime() {
        return new Stopwatch(Raylib::GetTime);
    }

    public static Stopwatch ofGameTime() {
        return new Stopwatch(GameLoop::getUnpausedTime);
    }

    public void start() {
        if (startTime == -1) startTime = timeSupp.getAsDouble();
    }

    public void stop() {
        startTime = -1;
    }

    public boolean hasElapsed(Duration duration) {
        return hasElapsedSeconds(duration.toMillis() / 1_000.0);
    }

    public boolean hasElapsedAdvance(Duration duration) {
        if (hasElapsedSeconds(duration.toMillis() / 1_000.0)) {
            restart();
            return true;
        }
        return false;
    }

    public boolean hasElapsedSeconds(double seconds) {
        return timeSupp.getAsDouble() > startTime + seconds;
    }

    public void restart() {
        startTime = timeSupp.getAsDouble();
    }

    public boolean hasElapsedSecondsAdvance(double seconds) {
        if (hasElapsedSeconds(seconds)) {
            restart();
            return true;
        }
        return false;
    }

    public long millisUntil(long millis) {
        long el = millisElapsed();
        return millis - el;
    }

    public long millisElapsed() {
        return (long) ((timeSupp.getAsDouble() - startTime)*1_000);
    }

    public boolean hasStarted() {
        return startTime != -1;
    }

    @Override
    public String toString() {
        return "Stopwatch(elapsed=" + (timeSupp.getAsDouble() - startTime) + ")";
    }
}
