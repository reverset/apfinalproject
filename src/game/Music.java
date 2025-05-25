package game;

import java.time.Duration;

import com.raylib.Raylib;

public class Music implements Resource {
    public final Signal<Void> onStart = new Signal<>();
    public final Signal<Void> onLoop = new Signal<>();

    private Raylib.Music internal = null;
    private String path;
    private boolean stopFlag = false;
    private boolean loop = false;

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
    public synchronized void init() {
        if (internal == null) {
            internal = Raylib.LoadMusicStream(path);
        }
    }

    @Override
    public synchronized void deinit() {
        if (internal == null) return;
        if (MusicManager.isThreadActive()) {
            stop();
            MusicManager.queueAction(() -> {
                Raylib.UnloadMusicStream(internal);
                internal = null;
            });
        } else {
            Raylib.UnloadMusicStream(internal);
            internal = null;
        }
    }

    @Override
    public String getResourcePath() {
        return path;
    }

    @Override
    public synchronized boolean isLoaded() {
        return internal != null && Raylib.IsMusicReady(internal);
    }

    public synchronized Music setLooping(boolean loop) {
        this.loop = loop;
        return this;
    }

    public synchronized boolean shouldLoop() {
        return loop;
    }

    public synchronized void play() {
        if (isPlaying()) return;
        stopFlag = false;
        MusicManager.play(this);
        onStart.emit(null);
    }

    public synchronized void play(Duration start) {
        if (isPlaying()) return;
        play();
        seek(start);
    }

    public synchronized void stop() {
        if (!isPlaying()) return;
        stopFlag = true;
        MusicManager.stop(this);
    }

    protected synchronized boolean isStopFlagRaised() {
        return stopFlag;
    }

    public synchronized boolean isPlaying() {
        return internal != null && Raylib.IsMusicStreamPlaying(internal);
    }

    public synchronized Duration getLength() {
        return Duration.ofMillis((int)(Raylib.GetMusicTimeLength(internal) * 1000));
    }

    public synchronized Duration getPlayedTime() {
        return Duration.ofMillis((int)(Raylib.GetMusicTimePlayed(internal) * 1000));
    }

    public synchronized float getPlayedTimeSeconds() {
        return Raylib.GetMusicTimePlayed(internal);
    }

    public synchronized float getLengthSeconds() {
        return Raylib.GetMusicTimeLength(internal);
    }

    public synchronized void seek(Duration duration) {
        Raylib.SeekMusicStream(internal, duration.toMillis() / 1_000f);
    }

    public synchronized boolean isFinished() {
        return MoreMath.isApprox(getPlayedTimeSeconds(), getLengthSeconds(), 0.02f);
    }

    public synchronized void setVolume(float normal) {
        if (normal > 1.0 || normal < 0) {
            throw new IllegalArgumentException();
        }
        Raylib.SetMusicVolume(internal, normal);
    }

    public synchronized void setPitch(float pitch) {
        Raylib.SetMusicPitch(internal, pitch);
    }

    public synchronized void setPan(float pan) {
        Raylib.SetMusicPan(internal, pan);
    }

    public Raylib.Music getPointer() {
        return internal;
    }
}
