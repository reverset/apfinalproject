package game;

import java.time.Duration;

import com.raylib.Raylib;

public class Music implements Resource {
    private Raylib.Music internal = null;
    private String path;
    private boolean stopFlag = false;

    public Music(Raylib.Music internal) {
        this.internal = internal;
    }

    public Music(String path) {
        this.path = path;
        internal = Raylib.LoadMusicStream(path);
    }

    public static Music newUnmanaged(String path) {
        Music m = new Music((Raylib.Music)null);
        m.path = path;
        return m;
    }

    @Override
    public void init() {
        if (internal == null) {
            internal = Raylib.LoadMusicStream(path);
        }
    }

    @Override
    public void deinit() {
        if (internal == null) return;
        Raylib.UnloadMusicStream(internal);
    }

    @Override
    public String getResourcePath() {
        return path;
    }

    @Override
    public boolean isLoaded() {
        return internal != null && Raylib.IsMusicReady(internal);
    }

    public void play() {
        if (isPlaying()) return;
        MusicManager.play(this);
    }

    public void stop() {
        if (!isPlaying()) return;
        stopFlag = true;
        MusicManager.stop(this);
    }

    protected boolean isStopFlagRaised() {
        return stopFlag;
    }

    public boolean isPlaying() {
        return Raylib.IsMusicStreamPlaying(internal);
    }

    public Duration getLength() {
        return Duration.ofMillis((int)(Raylib.GetMusicTimeLength(internal) * 1000));
    }

    public Duration getPlayedTime() {
        return Duration.ofMillis((int)(Raylib.GetMusicTimePlayed(internal) * 1000));
    }

    public void seek(Duration duration) {
        MusicManager.queueAction(() -> {
            Raylib.SeekMusicStream(internal, duration.toMillis() / 1_000f);
        });
    }

    public boolean isFinished() {
        return getPlayedTime().compareTo(getLength()) >= 0;
    }

    public void setVolume(float normal) {
        if (normal > 1.0 || normal < 0) {
            throw new IllegalArgumentException();
        }
        MusicManager.queueAction(() -> {
            Raylib.SetMusicVolume(internal, normal);
        });
    }

    public void setPitch(float pitch) {
        MusicManager.queueAction(() -> {
            Raylib.SetMusicPitch(internal, pitch);
        });
    }

    public void setPan(float pan) {
        MusicManager.queueAction(() -> {
            Raylib.SetMusicPan(internal, pan);
        });
    }

    public Raylib.Music getPointer() {
        return internal;
    }
}
