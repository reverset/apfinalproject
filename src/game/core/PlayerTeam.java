package game.core;

import java.util.List;
import java.util.Optional;

import com.raylib.Raylib.rAudioBuffer;

import game.RecoverableException;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public final class PlayerTeam extends Team {
    private static final PlayerTeam INSTANCE = new PlayerTeam();

    public static PlayerTeam get() {
        return INSTANCE;
    }

    @Override
    public Optional<Target> findTarget(Vec2 pos) {
        List<Entity> potentialTargets = getOpposingTeam().getMembers();
        for (final var pt : potentialTargets) {
            final var enemy = pt.getSystem(Enemy.class).orElseThrow(() -> new RecoverableException("Non-enemy on enemy team!"));
            
            if (enemy instanceof Cube cube && cube.isShieldActive()) continue;
            if (pos.distance(enemy.trans.position) > 400) continue;

            return Optional.of(Target.ofEntity(enemy.entity));
        }

        return Optional.empty();
    }

    @Override
    public Team getOpposingTeam() {
        return EnemyTeam.get();
    }

    @Override
    public boolean shouldEntityBeTargetted(Entity target) {
        return target.getSystem(Enemy.class)
            .filter(e -> e instanceof Cube cube && cube.isShieldActive())
            .isEmpty();
    }

    @Override
    public void grantExp(int xp) {}
}
