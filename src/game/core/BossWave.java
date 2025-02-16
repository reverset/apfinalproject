package game.core;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.ecs.Entity;

public class BossWave extends Wave {

    private Duration delay;
    private boolean started = false;
    private Stopwatch stopwatch = new Stopwatch();

    private boolean finished = false;
    private final int levelValue;

    public BossWave(Supplier<EntityOf<Enemy>> enemies, Duration delay, int levelValue) {
        super(enemies, 0, null);
        this.delay = delay;
        this.levelValue = levelValue;
    }

    @Override
    public void update() {
        if (!started && stopwatch.hasElapsed(delay)) {
            started = true;

            EntityOf<Enemy> enemy = enemies.get();
            spawnQueue.add(enemy);

            enemy.getMainSystem().health.onDeath.listen(n -> {
                finished = true;
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
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public int getLevelValue() {
        return levelValue;
    }
    
}
