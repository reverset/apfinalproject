package game;

import java.time.Duration;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import com.raylib.Raylib;


public class Stopwatch {
    double startTime = -1;

    public void start() {
        if (startTime == -1) startTime = Raylib.GetTime();
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
        return Raylib.GetTime() > startTime + seconds;
    }

    public void restart() {
        startTime = Raylib.GetTime();
    }

    public boolean hasElapsedSecondsAdvance(double seconds) {
        if (hasElapsedSeconds(seconds)) {
            restart();
            return true;
        }
        return false;
    }

    public boolean hasStarted() {
        return startTime != -1;
    }

    @Override
    public String toString() {
        return "Stopwatch(elapsed=" + (Raylib.GetTime() - startTime) + ")";
    }
}
