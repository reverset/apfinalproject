package game.core;

import java.time.Duration;
import java.util.Queue;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import game.EntityOf;
import game.Stopwatch;

public class Wave {
    public Queue<EntityOf<Square>> spawnQueue;
    public boolean waveStarted = false;
    
    EnemySpawner spawner;

    private Duration spawnRate;
    
    private Stopwatch timer = Stopwatch.ofGameTime();
    private int totalEnemies;
    
    Supplier<EntityOf<Square>> enemies;

    public Wave(Supplier<EntityOf<Square>> enemies, int totalEnemies, Duration spawnRate) {
        this.enemies = enemies;
        this.totalEnemies = totalEnemies;
        this.spawnRate = spawnRate;
    }

    public void update() {
        if (timer.hasElapsedAdvance(spawnRate)) {
            EntityOf<Square> enemy = enemies.get();
            spawnQueue.add(enemy);
        }
    }

    public void start() {}

    public boolean isFinished() {
        return (totalEnemies < spawner.getSpawnedEnemyCountForWave()) && (spawner.getEnemies().size() == 0);
    }

    public Wave clone() {
        return new Wave(enemies, totalEnemies, spawnRate);
    }
}
