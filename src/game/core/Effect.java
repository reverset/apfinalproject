package game.core;

import java.util.ArrayList;

import game.ecs.Component;

public class Effect implements Component {
    public interface WeaponLevelScale {
        int get(int dmg);
    }

    public interface WeaponDamageCalculator {
        int calculate(DamageInfo info);

        default DamageInfo compute(DamageInfo info) {
            return new DamageInfo(calculate(info), info.weapon());
        }
    }

    private int level = 1;
    WeaponLevelScale levelWeaponScale = (d) -> d;

    private ArrayList<WeaponDamageCalculator> damageScaling = new ArrayList<>();

    public int getLevel() {
        return level;
    }

    public Effect setLevel(int l) {
        level = l;
        return this;
    }

    @Deprecated
    public int getDesiredWeaponDamage(Weapon2 weapon, int damage) {
        return damage;
    }

    public DamageInfo computeDamage(DamageInfo info) {
        DamageInfo inf = info;
        for (var scale : damageScaling) {
            inf = scale.compute(info);
        }

        return inf;
    }

    // TODO
    public DamageInfo computeDamageResistance(DamageInfo info) {
        return info;
    }

    @Deprecated
    public Effect setLevelWeaponScalingFunction(WeaponLevelScale levelWeaponScale) {
        this.levelWeaponScale = levelWeaponScale;

        return this;
    }

    public Effect addDamageScaling(WeaponDamageCalculator scale) {
        damageScaling.add(scale);
        return this;
    }
}
