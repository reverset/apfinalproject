package game.core;

import java.util.Optional;

import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public final class EnemyTeam extends Team {
    private static final EnemyTeam INSTANCE = new EnemyTeam();

    private Optional<Unit> player = Optional.empty();

    public static EnemyTeam get() {
        return INSTANCE;
    }

    private Optional<Unit> getPlayer() {
        if (player.isEmpty()) {
            player = GameLoop.findEntityByTag(GameTags.PLAYER)
                .flatMap(p -> p.getSystem(Player.class));
            if (player.isPresent()) player.get().getEntity().onDestroy.listenOnce(n -> player = Optional.empty());
        }
        return player;
    }

    @Override
    public Optional<Unit> findTarget(Vec2 pos) {
        return getPlayer();
    }

    @Override
    public Team getOpposingTeam() {
        return PlayerTeam.get();
    }

    @Override
    public boolean shouldEntityBeTargetted(Entity target) {
        return true;
    }

    @Override
    public void grantExp(int xp) {
        getPlayer().ifPresent(pe -> pe.getEntity()
            .getSystem(Player.class)
            .ifPresent(p -> p.getExpAccumulator().accumulate(xp)));
    }

    @Override
    public Object[] getTeamTags() {
        return GameTags.ENEMY_TEAM_TAGS;
    }
}
