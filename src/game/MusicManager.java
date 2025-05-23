package game;

import com.raylib.Raylib;

public class MusicManager {
    private static AudioThread thread;

    public static AudioThread init() {
        thread = new AudioThread();
        thread.setDaemon(true);
        return thread;
    }

    public static void deinit() {
        thread.interrupt();
    }

    public static Music fromCacheOrLoad(String path) {
        return GameLoop.getResourceManager().getOrLoad(path, Music.class, () -> Music.newUnmanaged(path));
    }

    public static void play(Music music) {
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

    public static void queueAction(Runnable action) {
        thread.defer(action);
    }
}
