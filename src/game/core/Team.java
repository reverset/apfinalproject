package game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import game.Binded;
import game.Vec2;
import game.ecs.Entity;

public abstract sealed class Team permits EnemyTeam, PlayerTeam {
    private final List<Unit> members = new ArrayList<>();

    public static Team getTeamByTagOf(Entity entity) {
        if (entity.hasAnyTag(GameTags.PLAYER_TEAM_TAGS)) return PlayerTeam.get();
        if (entity.hasAnyTag(GameTags.ENEMY_TEAM_TAGS)) return EnemyTeam.get();
        return null;
    }

    public boolean isOnMyTeam(Unit other) {
        return members.contains(other);
    }

    public boolean isOnMyTeam(Entity other) {
        final var unit = other.getSystem(Unit.class);
        if (unit.isEmpty()) return false;
        return members.contains(unit.get());
    }

    public List<Unit> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void registerMember(Unit unit, boolean autoUnbind) {
        members.add(unit);

        if (autoUnbind) {
            unit.getEntity().bind(new Binded() {
                @Override
                public void unbind(Entity entity) {
                    final var u = entity.getSystem(Unit.class);
                    if (u.isEmpty()) return;
                    unregisterMember(u.get());
                }
            });
        }
    }

    public void unregisterMember(Unit unit) {
        members.remove(unit);
    }

    public abstract Optional<Unit> findTarget(Vec2 pos);
    public abstract Team getOpposingTeam();
    
    public abstract boolean shouldEntityBeTargetted(Entity target);

    public abstract void grantExp(int xp);
}
