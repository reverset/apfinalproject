package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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

    private int maxLevel = 1;
    
    private Queue<EntityOf<Unit>> spawnQueue = new LinkedList<>();

    private final Stopwatch stopwatch = Stopwatch.ofGameTime();

    private final Wave standardWave = new Wave(() -> {
        if (totalEnemiesThisWave > 5) return null;

        return randomEntity(getOffScreenPos());
    }, 5, Duration.ofSeconds(1));

    private Round round = new Round(List.of(standardWave.clone(),
        new BossWave(() -> HexagonWorm.makeEntity(getOffScreenPos(), maxLevel), Duration.ofSeconds(5), 10),
        standardWave.clone(),
        new BossWave(() -> Cube.makeEntity(getOffScreenPos(), maxLevel), Duration.ofSeconds(5), 10),
        standardWave.clone(),
        new BossWave(() -> TheRubinX.makeEntity(getOffScreenPos(), maxLevel), Duration.ofSeconds(5), 10)
    ), this);

    // private Round round = new Round(List.of(new Wave(() -> {
    //     if (totalEnemiesThisWave >= 1) return null;

    //     return Cube.makeEntity(Vec2.ZERO.screenToWorld(), maxLevel);
    // }, 1, Duration.ofSeconds(1))), this);

    // private Round round = new Round(List.of(new Wave(() -> {
    //     if (totalEnemiesThisWave >= 1) return null;

    //     return TheRubinX.makeEntity(Vec2.ZERO.screenToWorld(), maxLevel);
    // }, 1, Duration.ofSeconds(1))), this);


    public static Entity makeEntity() {
        return new Entity("EnemySpawner")
            .register(new EnemySpawner())
            .addTags("enemySpawner");
    }

    public int getSpawnedEnemyCountForWave() {
        return totalEnemiesThisWave;
    }

    public List<Entity> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public Queue<EntityOf<Unit>> getSpawnQueue() {
        return spawnQueue;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public EntityOf<Unit> randomEntity(Vec2 pos) {
        double rand = Math.random();
        int level = (int) Math.max(1, getMaxLevel() - (Math.random() * 4));
        if (rand > 0.8) {
            if (rand > 0.9) {
                return CircleEnemy.makeEntity(pos, level);
            }
            return TriangleEnemy.makeEntity(pos, level);
        }
        
        return Square.makeEntity(pos, level);
    }

    public static Vec2 getOffScreenPos() {
        Vec2 offset = Vec2.randomUnit().multiplyEq(Vec2.screen().x + Square.SIZE);
        return Vec2.screenCenter().screenToWorldEq().addEq(offset);
    }

    public void increaseLevel(int amount) {
        maxLevel += amount;
    }

    @Override
    public void frame() {
        boolean waveChange = round.update();
        if (waveChange) {
            stopwatch.start();
            totalEnemiesThisWave = 0;
        }

        if (!spawnQueue.isEmpty()) {
            EntityOf<Unit> enemy = spawnQueue.poll();
            if (enemy == null) return;

            GameLoop.safeTrack(enemy);
            enemies.add(enemy);
            totalEnemiesThisWave += 1;
            enemy.getMainSystem().getHealth().onDeath.listen(n -> {
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
