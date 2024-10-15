package game.core;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

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

    private ArrayList<DamageCalculator> damageScaling = new ArrayList<>();
    private ArrayList<DamageCalculator> damageResponse = new ArrayList<>();
    private ArrayList<Powerup> powerups = new ArrayList<>();

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

        for (var power : powerups) {
            inf = power.outgoingDamageMod(info);
        }

        return inf;
    }

    public DamageInfo computeDamageResistance(DamageInfo info) {
        DamageInfo inf = info;
        for (var scale : damageResponse) {
            inf = scale.compute(info);
        }

        for (var power : powerups) {
            inf = power.incomingDamageMod(info);
        }

        return inf;
    }

    public Effect addDamageScaling(DamageCalculator scale) {
        damageScaling.add(scale);
        return this;
    }

    public Effect addDamageRecievingResponse(DamageCalculator scale) {
        damageResponse.add(scale);
        return this;
    }

    public Effect addDamageRecievingResponseExtra(Function<DamageInfo, DamageInfo> func) {
        var calc = new DamageCalculator() {

            @Override
            public int calculate(DamageInfo info) {
                return func.apply(info).damage();
            }

            @Override
            public DamageInfo compute(DamageInfo info) {
                return func.apply(info);
            }
            
        };

        damageResponse.add(calc);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Powerup> Optional<T> getPowerUp(Class<T> clazz) {
        for (var power: powerups) {
            if (power.getClass().equals(clazz)) {
                return Optional.of((T) power);
            }
        }
        return Optional.empty();
    }


    public <T extends Powerup> boolean hasPowerUp(Class<T> clazz) {
        return getPowerUp(clazz).isPresent();
    }

    public <T extends Powerup> boolean hasPowerUpThenIncrementLevel(Class<T> clazz) {
        Optional<T> power = getPowerUp(clazz);

        power.ifPresent(p -> {
            p.level += 1;
        });

        return power.isPresent();
    }

    public Effect registerPowerup(Powerup powerup) {
        powerups.add(powerup);
        return this;
    }

    public Effect unregisterPowerup(Powerup powerup) {
        powerups.remove(powerup);
        return this;
    }
}
