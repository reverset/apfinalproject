package game.core;

import java.util.ArrayList;

import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.Entity;

public class EnemySpawner extends ECSystem {
    private final Stopwatch stopwatch = new Stopwatch();
    private final ArrayList<Entity> enemies = new ArrayList<>();
    private final Stopwatch levelIncrease = new Stopwatch();

    private int maxLevel = 1;

    public static Entity makeEntity() {
        return new Entity("EnemySpawner")
            .register(new EnemySpawner());
    }

    public EntityOf<Enemy> randomEntity(Vec2 pos) {
        double rand = Math.random();
        int level = (int) Math.max(1, maxLevel - (Math.random() * 4));
        if (rand > 0.8) {
            if (rand > 0.9) {
                return CircleEnemy.makeEntity(pos, level);
            }
            return TriangleEnemy.makeEntity(pos, level);
        }
        
        return Enemy.makeEntity(pos, level);

    }

    @Override
    public void frame() {
        if (enemies.size() < 10 && stopwatch.hasElapsedSecondsAdvance(0.5)) {
            Vec2 offset = Vec2.randomUnit().multiplyEq(Vec2.screen().x + Enemy.SIZE);
            Vec2 spawnPosition = Vec2.screenCenter().screenToWorldEq().addEq(offset);
            // Vec2 spawnPosition = Vec2.screenCenter().screenToWorldEq();
            EntityOf<Enemy> entity = randomEntity(spawnPosition);
            
            enemies.add(entity);
            GameLoop.safeTrack(entity);
            GameLoop.defer(() -> {
                entity.onDestroy.listenOnce(n -> {
                    enemies.remove(entity);
                });
            });
        }

        if (levelIncrease.hasElapsedSecondsAdvance(5)) {
            maxLevel += 1;
        }
    }

    @Override
    public void setup() {
    }
}
