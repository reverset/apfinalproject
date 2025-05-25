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
        try {
            while (!isInterrupted()) {
                loop();
                runDeferred();
                controlTiming();
            }
        } catch (InterruptedException e) {
            System.out.println("MusicManger interrupted... shutting down.");
        }
    }

    public void stopAll() {
        lock.lock();
        try {
            musicList.forEach(Music::stop);
        } finally {
            lock.unlock();
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
            while (iter.hasNext() && !isInterrupted()) {
                final var m = iter.next();

                synchronized (m) {
                    boolean fin = m.isFinished();
                    boolean loop = m.shouldLoop();
                    if (m.getPointer() == null || m.isStopFlagRaised() || (fin && !loop) || !m.isPlaying()) iter.remove();
                    else {
                        if (fin && loop) m.onLoop.emit(null);
    
                        Raylib.UpdateMusicStream(m.getPointer());
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void controlTiming() throws InterruptedException {
        Thread.sleep(16); // 60 FPS
    }
}
