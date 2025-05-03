package game.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import game.Binded;
import game.Vec2;
import game.ecs.Entity;

public abstract sealed class Team permits EnemyTeam, PlayerTeam {
    private final List<Entity> members = new ArrayList<>();

    public static Team getTeamByTagOf(Entity entity) {
        if (entity.hasAnyTag(GameTags.PLAYER_TEAM_TAGS)) return PlayerTeam.get();
        if (entity.hasAnyTag(GameTags.ENEMY_TEAM_TAGS)) return EnemyTeam.get();
        return null;
    }

    public List<Entity> getMembers() {
        return members;
    }

    public void registerMember(Entity entity, boolean autoUnbind) {
        members.add(entity);

        if (autoUnbind) {
            entity.bind(new Binded() {
                @Override
                public void unbind(Entity entity) {
                    unregisterMember(entity);
                }
            });
        }
    }

    public void unregisterMember(Entity entity) {
        members.remove(entity);
    }

    public abstract Optional<Target> findTarget(Vec2 pos);
    public abstract Team getOpposingTeam();
    
    public abstract boolean shouldEntityBeTargetted(Entity target);
}
