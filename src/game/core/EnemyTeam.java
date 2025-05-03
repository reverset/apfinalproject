package game.core;

import java.util.Optional;

import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public final class EnemyTeam extends Team {
    private static final EnemyTeam INSTANCE = new EnemyTeam();

    private Optional<Target> player = Optional.empty();

    public static EnemyTeam get() {
        return INSTANCE;
    }

    private Optional<Target> getPlayer() {
        if (player.isEmpty()) {
            player = GameLoop.findEntityByTag(GameTags.PLAYER)
                .map(Target::ofEntity);
            if (player.isPresent()) player.get().entity().onDestroy.listenOnce(n -> player = Optional.empty());
        }
        return player;
    }

    @Override
    public Optional<Target> findTarget(Vec2 pos) {
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
        getPlayer().get().entity()
            .getSystem(Player.class)
            .ifPresent(p -> p.getExpAccumulator().accumulate(xp));
    }
}
