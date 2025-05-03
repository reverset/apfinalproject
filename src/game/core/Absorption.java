package game.core;

import game.ecs.Entity;

public class Absorption extends Powerup {
    public Absorption(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        super.setup();

        effect.addDamageRecievingResponse(this::calculateDamage);
    }

    private int calculateDamage(DamageInfo info) {
        if (Math.random() < getRandomChance()) return 0;

        return Math.max((int) (info.damage() * (1 - getPercentage())), 1);
    }

    @Override
    public int getMaxLevel() {
        return 6;
    }

    @Override
    public void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Absorption";
    }

    @Override
    public String getDescription() {
        return "Absorb a percentage of\nincoming damage, with a\nchance to absorb\nall of it.";
    }

    @Override
    public String getSmallHUDInfo() {
        return ((int) (getPercentage()*100)) + "% absorption";
    }

    private double getPercentage() {
        return (1.0/10) * level;
    }

    private double getRandomChance() {
        return 0.2;
    }
    
}
