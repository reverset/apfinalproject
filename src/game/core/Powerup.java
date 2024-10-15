package game.core;

import game.ecs.ECSystem;
import game.ecs.Entity;

public abstract class Powerup extends ECSystem {
    protected Entity entity;
    protected Weapon2 weapon;
    protected Effect effect;
    protected int level;

    public Powerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        this.entity = entity;
        this.weapon = weapon;
        this.effect = effect;
        this.level = level;
    }

    public abstract int getMaxLevel();
    public abstract void levelUp();

    public abstract String getName();
    public abstract String getDescription();

    public boolean canLevelUp() {
        return level < getMaxLevel();
    }

    @Override
    public void setup() {
        effect.registerPowerup(this);
    }

    @Override
    public void destroy() {
        effect.unregisterPowerup(this);
    }

    public DamageInfo outgoingDamageMod(DamageInfo info) {
        return info;
    }

    public DamageInfo incomingDamageMod(DamageInfo info) {
        return info;
    }

    public Powerup setWeapon(Weapon2 weapon) {
        this.weapon = weapon;
        return this;
    }
}
