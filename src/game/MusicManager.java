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
        Raylib.CloseAudioDevice();
    }

    public static Music fromCacheOrLoad(String path) {
        return GameLoop.getResourceManager().getOrLoad(path, Music.class, () -> Music.newUnmanaged(path));
    }

    public static void play(Music music) {
        if (!Settings.musicEnabled) return; // TODO

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
