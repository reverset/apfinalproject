package game.core;

import game.ecs.Component;

public class Effect implements Component {
    public interface WeaponLevelScale {
        int get(int dmg);
    }

    private int level = 1;
    WeaponLevelScale levelWeaponScale = (d) -> d;

    public int getLevel() {
        return level;
    }

    public Effect setLevel(int l) {
        level = l;
        return this;
    }

    public int getDesiredWeaponDamage(Weapon2 weapon, int damage) {
        return levelWeaponScale.get(damage);
    }

    public Effect setLevelWeaponScalingFunction(WeaponLevelScale levelWeaponScale) {
        this.levelWeaponScale = levelWeaponScale;

        return this;
    }
}
