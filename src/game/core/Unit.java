package game.core;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public abstract class Unit extends ECSystem {
    private Transform trans = null;
    private Tangible tangible = null;
    private Health health = null;
    private Effect effect = null;
    private Team team = null;

    public Transform getTransform() {
        if (trans == null) {
            trans = require(Transform.class);
        }
        return trans;
    }

    public Tangible getTangible() {
        if (tangible == null) {
            tangible = require(Tangible.class);
        }
        return tangible;
    }

    public Health getHealth() {
        if (health == null) {
            health = require(Health.class);
        }
        return health;
    }

    public Effect getEffect() {
        if (effect == null) {
            effect = require(Effect.class);
        }
        return effect;
    }

    public Team getTeam() {
        if (team == null) {
            team = Team.getTeamByTagOf(entity);
        }
        return team;
    }

    public void setTeam(Team team) {
        this.team.unregisterMember(entity);
        this.team = team;
        this.team.registerMember(entity, true);
    }

    public boolean isBossEnemy() {
        return false;
    }
}
