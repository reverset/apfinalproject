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
    private boolean init = false;
    private boolean messageShown = false;
    private Stopwatch stopwatch = new Stopwatch();

    private boolean finished = false;

    public BossWave(Supplier<EntityOf<Enemy>> enemies, Duration delay) {
        super(enemies, null, null);
        this.delay = delay;
    }

    @Override
    public void update() { // add method for the start of a wave.
        if (!init) {
            stopwatch.start();
            init = true;
        }

        if (!started && stopwatch.hasElapsed(delay)) {
            started = true;

            EntityOf<Enemy> enemy = enemies.get();
            spawnQueue.add(enemy);

            enemy.getMainSystem().health.onDeath.listen(n -> {
                finished = true;
            }, enemy);

        } else if (!messageShown) {
            messageShown = true;
            Optional<Entity> playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);
            if (playerEntity.isEmpty()) {
                finished = true;
                return;
            }

            Player player = playerEntity.get().getSystem(Player.class).orElseThrow();
            player.animatedWarningNotif("A BOSS APPROACHES", 3);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
    
}
