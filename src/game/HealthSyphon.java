package game;

import java.time.Duration;

import com.raylib.Raylib;

import game.core.DamageInfo;
import game.core.Effect;
import game.core.EnemySpawner;
import game.core.Health;
import game.core.Powerup;
import game.core.Weapon2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class HealthSyphon extends Powerup {
    private Transform trans;
    private Color color = new Color(0, 255, 0, 200);
    private float range = 300;
    private EnemySpawner spawner;
    private Health health;

    private final static Duration STEAL_INTERVAL = Duration.ofMillis(250); 

    private Stopwatch stealStopwatch = Stopwatch.ofGameTime();

    public HealthSyphon(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        super.setup();
        trans = require(Transform.class);
        health = require(Health.class);
    }

    @Override
    public void ready() {
        super.ready();
        spawner = GameLoop.findEntityByTag("enemySpawner")
            .flatMap(e -> e.getSystem(EnemySpawner.class))
            .orElseThrow(() -> new RecoverableException("no enemy spawner for health syphon."));
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public void levelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Health Syphon";
    }

    @Override
    public String getDescription() {
        return "Steal health from nearby\nenemies.";
    }

    @Override
    public void render() {
        super.render();
        Raylib.DrawCircleLines(trans.position.xInt(), trans.position.yInt(), range, color.getPointerNoUpdate());
    }

    @Override
    public void infrequentUpdate() {
        super.infrequentUpdate();

        if (!stealStopwatch.hasElapsedAdvance(STEAL_INTERVAL)) return;

        for (final var enemy : spawner.getEnemies()) {
            enemy.getComponent(Transform.class).ifPresent(et -> {
                if (et.position.distance(trans.position) <= range) {
                    enemy.getComponent(Health.class).ifPresent(h -> {
                        DamageInfo info = new DamageInfo(getHealthSteal(), enemy, weapon, et.position.clone()).setAttacker(entity);
                        h.damage(info);
                        health.heal(info.asHealing().setPosition(trans.position.clone()));
                    });
                }
            });
        }
    }

    @Override
    public String getSmallHUDInfo() {
        return getHealthSteal() + " health steal";
    }

    private int getHealthSteal() {
        return level;
    }
    
}
