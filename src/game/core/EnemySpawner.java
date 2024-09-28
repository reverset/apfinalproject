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

    public static Entity makeEntity() {
        return new Entity("EnemySpawner")
            .register(new EnemySpawner());
    }

    @Override
    public void frame() {
        if (enemies.size() < 10 && stopwatch.hasElapsedSecondsAdvance(0.5)) {
            Vec2 offset = Vec2.randomUnit().multiplyEq(Vec2.screen().x + Enemy.SIZE);
            Vec2 spawnPosition = Vec2.screenCenter().screenToWorldEq().addEq(offset);
            // EntityOf<Enemy> entity = Enemy.makeEntity(spawnPosition);
            EntityOf<Enemy> entity = CircleEnemy.makeEntity(spawnPosition);
            
            enemies.add(entity);
            GameLoop.safeTrack(entity);
            GameLoop.defer(() -> {
                entity.getMainSystem().health.onDeath.listen(n -> {
                    enemies.remove(entity);
                }, entity);
            });
        }
    }

    @Override
    public void setup() {
    }
}
