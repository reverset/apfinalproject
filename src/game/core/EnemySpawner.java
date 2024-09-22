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
        if (enemies.size() < 5 && stopwatch.hasElapsedSecondsAdvance(5)) {
            EntityOf<Enemy> entity = Enemy.makeEntity(Vec2.random(
                Vec2.zero().screenToWorldEq().minus(100),
                Vec2.screen().screenToWorldEq().add(100)));
            
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
