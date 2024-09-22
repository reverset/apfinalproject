package game;

import com.raylib.Raylib;


public class Stopwatch {
    private double startTime = -1;

    public void start() {
        if (startTime == -1) startTime = Raylib.GetTime();
    }

    public void stop() {
        startTime = -1;
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
}
