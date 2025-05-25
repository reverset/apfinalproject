package game;

import com.raylib.Raylib;

import game.core.Settings;

public class MusicManager {
    private static AudioThread thread;

    public static AudioThread init() {
        Raylib.InitAudioDevice();
        thread = new AudioThread();
        thread.setDaemon(true);

        thread.start();
        return thread;
    }

    public static void deinit() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Raylib.CloseAudioDevice();
    }

    public static boolean isThreadActive() {
        return thread != null && !thread.isInterrupted() && thread.isAlive();
    }

    public static Music fromCacheOrLoad(String path) {
        return GameLoop.getResourceManager().getOrLoad(path, Music.class, () -> Music.newUnmanaged(path));
    }

    public static void play(Music music) {
        if (!Settings.isMusicEnabled()) return;

        queueAction(() -> {
            Raylib.PlayMusicStream(music.getPointer());
            thread.track(music);
        });
    }

    public static void stop(Music music) {
        queueAction(() -> {
            Raylib.StopMusicStream(music.getPointer());
        });
    }

    public static void stopAll() {
        thread.stopAll();
    }

    public static void queueAction(Runnable action) {
        thread.defer(action);
    }
}
