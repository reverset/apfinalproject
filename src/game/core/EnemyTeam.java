package game.core;

import java.util.Optional;

import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

// TODO, finish making enemies target using their Team rather than the player directly.
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
}
