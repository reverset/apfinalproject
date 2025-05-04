package game.core;

import java.util.List;
import java.util.Optional;

import game.Vec2;
import game.ecs.Entity;

public final class PlayerTeam extends Team {
    private static final PlayerTeam INSTANCE = new PlayerTeam();

    public static PlayerTeam get() {
        return INSTANCE;
    }

    @Override
    public Optional<Unit> findTarget(Vec2 pos) {
        List<Unit> potentialTargets = getOpposingTeam().getMembers();
        for (final var pt : potentialTargets) {

            if (pt instanceof Cube cube && cube.isShieldActive()) continue;
            if (pos.distance(pt.getTransform().position) > 400) continue;

            return Optional.of(pt);
        }

        return Optional.empty();
    }

    @Override
    public Team getOpposingTeam() {
        return EnemyTeam.get();
    }

    @Override
    public boolean shouldEntityBeTargetted(Entity target) {
        return target.getSystem(Unit.class)
            .filter(e -> e instanceof Cube cube && cube.isShieldActive() || e.getHealth().isDead())
            .isEmpty();
    }

    @Override
    public void grantExp(int xp) {}
}
