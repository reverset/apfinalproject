package game.core;

import game.ecs.ECSystem;
import game.ecs.Entity;

public abstract class Powerup extends ECSystem {
    protected Entity entity;
    protected Weapon2 weapon;
    protected Effect effect;
    protected int level;

    // private int incomingDamageModPriority = 0;
    // private int outgoingDamageModPriority = 0;

    private int priority = 0;

    public Powerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        this.entity = entity;
        this.weapon = weapon;
        this.effect = effect;
        this.level = level;
    }

    public abstract int getMaxLevel();
    protected abstract void doLevelUp();
    
    public void levelUp() {
        if (level < getMaxLevel()) doLevelUp();
    }

    public abstract String getName();
    public abstract String getDescription();

    // public void setIncomingDamagePriority(int value) {
    //     incomingDamageModPriority = value;
    // }

    // public void setOutgoingDamagePriority(int value) {
    //     outgoingDamageModPriority = value;
    // }

    // public int getIncomingDamagePriority() {
    //     return incomingDamageModPriority;
    // }

    // public void getOutgoingDamagePriority() {
    //     return outgoingDamageModPriority;
    // }

    public void setPriority(int value) {
        priority = value;
    }

    public int getPriority() {
        return priority;
    }

    public boolean canLevelUp() {
        return level < getMaxLevel();
    }

    public String getSmallHUDInfo() {
        return "";
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
