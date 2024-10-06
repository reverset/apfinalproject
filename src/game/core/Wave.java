package game.core;

import java.time.Duration;
import java.util.Queue;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import game.EntityOf;
import game.Stopwatch;

public class Wave {
    public Queue<EntityOf<Enemy>> spawnQueue;

    private Duration spawnRate;
    
    private Stopwatch timer = new Stopwatch();
    private BooleanSupplier finish;
    
    Supplier<EntityOf<Enemy>> enemies;

    public Wave(Supplier<EntityOf<Enemy>> enemies, BooleanSupplier finish, Duration spawnRate) {
        this.enemies = enemies;
        this.finish = finish;
        this.spawnRate = spawnRate;
    }

    public void update() {
        if (timer.hasElapsedAdvance(spawnRate)) {
            EntityOf<Enemy> enemy = enemies.get();
            spawnQueue.add(enemy);
        }
    }

    public boolean isFinished() {
        return finish.getAsBoolean();
    }
}
