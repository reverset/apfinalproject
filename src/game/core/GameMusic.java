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
    }

    public void transitionToBoss() {
        GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
            mainMenuMusic.setVolume(val);
        }).start().runWhilePaused(true).onFinish.listenOnce(n -> {
            mainMenuMusic.stop();
            mainMenuMusic.setVolume(1); // so when it is restarted, the volume is normal.
        });
        bossMusic.play();
    }

    public void transitionToMenu() {
        GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
            bossMusic.setVolume(val);
        }).start().runWhilePaused(true).onFinish.listenOnce(n -> {
            bossMusic.stop();
            bossMusic.setVolume(1); // so when it is restarted, the volume is normal.
        });
        mainMenuMusic.play(mainMenuStart);
    }

    public void transitionToQuiet() {
        if (mainMenuMusic.isPlaying()) {
            GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
                mainMenuMusic.setVolume(val);
            }).start().runWhilePaused(true).onFinish.listenOnce(n -> {
                mainMenuMusic.stop();
                mainMenuMusic.setVolume(1); // so when it is restarted, the volume is normal.
            });
        }
        if (bossMusic.isPlaying()) {
            GameLoop.makeTween(Tween.lerp(1, 0), 3, val -> {
                bossMusic.setVolume(val);
            }).start().runWhilePaused(true).onFinish.listenOnce(n -> {
                bossMusic.stop();
                bossMusic.setVolume(1); // so when it is restarted, the volume is normal.
            });
        }
    }
    
}
