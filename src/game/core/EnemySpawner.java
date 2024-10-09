package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.Entity;

public class EnemySpawner extends ECSystem {
    private final ArrayList<Entity> enemies = new ArrayList<>();
    private int totalEnemiesThisWave = 0;

    private final Stopwatch levelIncrease = new Stopwatch();
    
    private int maxLevel = 1;
    
    private Queue<EntityOf<Enemy>> spawnQueue = new LinkedList<>();

    private final Stopwatch stopwatch = new Stopwatch();

    private Round round = new Round(List.of(new Wave(() -> {
        if (totalEnemiesThisWave > 5) return null;

        return randomEntity(getOffScreenPos());
    }, 5, Duration.ofSeconds(1)),
    new BossWave(() -> BossEnemy.makeEntity(getOffScreenPos(), maxLevel), Duration.ofSeconds(5))), this);

    public static Entity makeEntity() {
        return new Entity("EnemySpawner")
            .register(new EnemySpawner());
    }

    public int getSpawnedEnemyCountForWave() {
        return totalEnemiesThisWave;
    }

    public ArrayList<Entity> getEnemies() {
        return enemies;
    }

    public Queue<EntityOf<Enemy>> getSpawnQueue() {
        return spawnQueue;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public EntityOf<Enemy> randomEntity(Vec2 pos) {
        double rand = Math.random();
        int level = (int) Math.max(1, getMaxLevel() - (Math.random() * 4));
        if (rand > 0.8) {
            if (rand > 0.9) {
                return CircleEnemy.makeEntity(pos, level);
            }
            return TriangleEnemy.makeEntity(pos, level);
        }
        
        return Enemy.makeEntity(pos, level);
    }

    public static Vec2 getOffScreenPos() {
        Vec2 offset = Vec2.randomUnit().multiplyEq(Vec2.screen().x + Enemy.SIZE);
        return Vec2.screenCenter().screenToWorldEq().addEq(offset);
    }

    @Override
    public void frame() {
        if (levelIncrease.hasElapsedSecondsAdvance(5)) {
            maxLevel += 1;
        }
        
        boolean waveChange = round.update();
        if (waveChange) {
            stopwatch.start();
            totalEnemiesThisWave = 0;
        }

        if (!spawnQueue.isEmpty()) {
            EntityOf<Enemy> enemy = spawnQueue.poll();
            if (enemy == null) return;

            GameLoop.safeTrack(enemy);
            enemies.add(enemy);
            totalEnemiesThisWave += 1;
            enemy.getMainSystem().health.onDeath.listen(n -> {
                enemies.remove(enemy);
            }, enemy);
        }
        
    }

    @Override
    public void setup() {
        stopwatch.start();
        round.getWave().start();
    }
}
