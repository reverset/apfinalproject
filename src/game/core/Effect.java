package game.core;

import java.util.ArrayList;

import game.ecs.Component;

public class Effect implements Component {
    public interface WeaponLevelScale {
        int get(int dmg);
    }

    public interface DamageCalculator {
        int calculate(DamageInfo info);

        default DamageInfo compute(DamageInfo info) {
            return info.setDamage(calculate(info));
        }
    }

    private int level = 1;
    WeaponLevelScale levelWeaponScale = (d) -> d;

    private ArrayList<DamageCalculator> damageScaling = new ArrayList<>();
    private ArrayList<DamageCalculator> damageResponse = new ArrayList<>();

    public int getLevel() {
        return level;
    }

    public Effect setLevel(int l) {
        level = l;
        return this;
    }

    public DamageInfo computeDamage(DamageInfo info) {
        DamageInfo inf = info;
        for (var scale : damageScaling) {
            inf = scale.compute(info);
        }

        return inf;
    }

    public DamageInfo computeDamageResistance(DamageInfo info) {
        DamageInfo inf = info;
        for (var scale : damageResponse) {
            inf = scale.compute(info);
        }

        return inf;
    }

    @Deprecated
    public Effect setLevelWeaponScalingFunction(WeaponLevelScale levelWeaponScale) {
        this.levelWeaponScale = levelWeaponScale;

        return this;
    }

    public Effect addDamageScaling(DamageCalculator scale) {
        damageScaling.add(scale);
        return this;
    }

    public Effect addDamageRecievingResponse(DamageCalculator scale) {
        damageResponse.add(scale);
        return this;
    }
}
