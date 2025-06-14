package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import game.EntityOf;
import game.GameLoop;
import game.Music;
import game.MusicManager;
import game.Tween;
import game.ecs.ECSystem;

public class GameMusic extends ECSystem {

    private static EntityOf<GameMusic> instance = null;

    private Music mainMenuMusic = null; // ugly but whatever, EOL for this game now.
    private Duration mainMenuStart = Duration.ofSeconds(0);
    private Music bossMusic = null;
    private Duration bossStart = Duration.ofSeconds(0);
    
    private Music deathMusic = null;
    private Music baseTheme = null;

    private List<Music> songs = new ArrayList<>();

    public static EntityOf<GameMusic> get() {
        if (instance == null) {
            instance = new EntityOf<>("Game Music", GameMusic.class);
            instance.register(new GameMusic());
            GameLoop.safeTrack(instance);
        }
        return instance;
    }

    private Music loadSong(String path) {
        final var song = MusicManager.fromCacheOrLoad(path);
        if (song == null) return null;

        songs.add(song);

        return song;
    }

    private void playOnly(Music music, Duration start) {
        if (!Settings.isMusicEnabled()) return;
        if (music.isPlaying()) return;
        final var others = songs.stream().filter(m -> m != music).toList();
        
        for (final var song : others) {
            if (song.isPlaying()) {
                final var tween = GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
                    song.setVolume(val);
                }).start();
                tween.entity.setDestructibility(false);
                tween.runWhilePaused(true).onFinish.listenOnce(n -> {
                    song.stop();
                });
            }
        }

        music.play(start);
        final var tween = GameLoop.makeTween(Tween.lerp(0, 1), 3, val -> {
            music.setVolume(val);
        }).start().runWhilePaused(true);
        tween.entity.setDestructibility(false);
    }

    @Override
    public void setup() {
        entity.setDestructibility(false);

        mainMenuMusic = loadSong("resources/mainMenu.mp3").setLooping(true);
        mainMenuMusic.onLoop.listen(n -> { // old but too lazy to remove
            mainMenuMusic.seek(mainMenuStart);
        });

        mainMenuMusic.play(mainMenuStart);

        bossMusic = loadSong("resources/boss.mp3").setLooping(true);
        deathMusic = loadSong("resources/death.mp3");

        baseTheme = loadSong("resources/basetheme.mp3").setLooping(true);

        Settings.onMusicEnableChange.listen(enabled -> {
            if (!enabled) {
                mainMenuMusic.setVolume(0);
                bossMusic.setVolume(0);
            } else {
                mainMenuMusic.setVolume(1);
                bossMusic.setVolume(1);
            }
        }, entity);
    }

    public void transitionToBoss() {
        playOnly(bossMusic, bossStart);
    }

    public void transitionToMenu() {
        playOnly(mainMenuMusic, mainMenuStart);
    }

    public void transitionToBaseTheme() {
        playOnly(baseTheme, Duration.ofSeconds(0));
    }

    public void transitionToDeath() {
        playOnly(deathMusic, Duration.ofSeconds(0));
    }
}
