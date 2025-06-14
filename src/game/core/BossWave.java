package game.core;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import game.EntityOf;
import game.GameLoop;
import game.Music;
import game.MusicManager;
import game.Stopwatch;
import game.ecs.Entity;

public class BossWave extends Wave {

    private Duration delay;
    private boolean started = false;
    private Stopwatch stopwatch = Stopwatch.ofGameTime();

    private boolean finished = false;
    private final int levelValue;

    public BossWave(Supplier<EntityOf<Unit>> enemies, Duration delay, int levelValue) {
        super(enemies, 0, null);
        this.delay = delay;
        this.levelValue = levelValue;
    }

    @Override
    public void update() {
        if (!started && stopwatch.hasElapsed(delay)) {
            started = true;

            EntityOf<Unit> enemy = enemies.get();
            spawnQueue.add(enemy);

            enemy.getMainSystem().getHealth().onDeath.listen(n -> {
                finished = true;
                GameMusic.get().getMainSystem().transitionToBaseTheme();
            }, enemy);

        }
    }

    @Override
    public void start() {
        stopwatch.restart();
        finished = false;
        started = false;

        Optional<Entity> playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);
        if (playerEntity.isEmpty()) {
            finished = true;
            return;
        }

        Player player = playerEntity.get().getSystem(Player.class).orElseThrow();
        player.animatedWarningNotif("A BOSS APPROACHES", 3);

        GameMusic.get().getMainSystem().transitionToBoss();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public int getLevelValue() {
        return levelValue;
    }
    
}
