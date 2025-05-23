package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.raylib.Raylib;

public class AudioThread extends Thread {
    private final ArrayList<Music> musicList = new ArrayList<>();
    
    // guards musicList
    private final ReentrantLock lock = new ReentrantLock();

    private final List<Runnable> deferments = Collections.synchronizedList(new LinkedList<>());

    public void defer(Runnable action) {
        deferments.add(action);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            loop();
            runDeferred();
            controlTiming();
        }
    }

    private void runDeferred() {
        if (deferments.size() == 0) return;
        synchronized (deferments) {
            final var action = deferments.remove(0);
            action.run();
        }
    }

    public void track(Music music) {
        lock.lock();
        try {
            musicList.add(music);
        } finally {
            lock.unlock();
        }
    }

    private void loop() {
        lock.lock();
        try {
            final var iter = musicList.listIterator();
            while (iter.hasNext()) {
                final var m = iter.next();
                
                if (m.isStopFlagRaised()) iter.remove();
                else Raylib.UpdateMusicStream(m.getPointer());
            }
        } finally {
            lock.unlock();
        }
    }

    private void controlTiming() {
        try {
            Thread.sleep(16); // 60 FPS
        } catch (InterruptedException e) {}
    }
}
