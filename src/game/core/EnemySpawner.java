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

    public static EntityOf<Enemy> randomEntity(Vec2 pos) {
        // double rand = Math.random();
        // if (rand < 0.9) {
        //     return Enemy.makeEntity(pos);
        // }

        // return CircleEnemy.makeEntity(pos);

        return TriangleEnemy.makeEntity(pos);
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
    }

    @Override
    public void setup() {
    }
}
