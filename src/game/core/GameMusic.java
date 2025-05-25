package game.core;

import java.time.Duration;

import game.EntityOf;
import game.GameLoop;
import game.Music;
import game.MusicManager;
import game.Tween;
import game.ecs.ECSystem;

public class GameMusic extends ECSystem {

    private static EntityOf<GameMusic> instance = null;

    private Music mainMenuMusic = null; // ugly but whatever, one day ill improve
    private Duration mainMenuStart = Duration.ofSeconds(45).plus(Duration.ofMillis(600));
    private Music bossMusic = null;
    private Duration bossStart = Duration.ofSeconds(3);

    public static EntityOf<GameMusic> get() {
        if (instance == null) {
            instance = new EntityOf<>("Game Music", GameMusic.class);
            instance.register(new GameMusic());
            GameLoop.safeTrack(instance);
        }
        return instance;
    }

    @Override
    public void setup() {
        entity.setDestructibility(false);

        mainMenuMusic = MusicManager.fromCacheOrLoad("resources/mainMenu.mp3").setLooping(true);
        mainMenuMusic.onLoop.listen(n -> {
            mainMenuMusic.seek(mainMenuStart);
        });

        mainMenuMusic.play(mainMenuStart);

        bossMusic = MusicManager.fromCacheOrLoad("resources/boss.mp3");

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
        if (!Settings.isMusicEnabled()) return;
        if (bossMusic.isPlaying()) return;

        GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
            mainMenuMusic.setVolume(val);
        }).start().runWhilePaused(true).onFinish.listenOnce(n -> {
            mainMenuMusic.stop();
        });

        bossMusic.play(bossStart);
        GameLoop.makeTween(Tween.lerp(0, 1), 3, val -> {
            bossMusic.setVolume(val);
        }).start().runWhilePaused(true);
    }

    public void transitionToMenu() {
        if (!Settings.isMusicEnabled()) return;
        if (mainMenuMusic.isPlaying()) return;

        Tween<Float> tween = GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
            bossMusic.setVolume(val);
        }).start().runWhilePaused(true);
        tween.entity.setDestructibility(false); // avoid tween being destroyed during scene changes.

        tween.onFinish.listenOnce(n -> {
            bossMusic.stop();
        });

        mainMenuMusic.play(mainMenuStart);
        GameLoop.makeTween(Tween.lerp(0, 1), 3, val -> {
            mainMenuMusic.setVolume(val);
        }).start().runWhilePaused(true).entity.setDestructibility(false);;
        
    }
}
