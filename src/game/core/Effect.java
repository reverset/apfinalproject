package game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import game.Signal;
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
    
    public final Signal<Integer> onLevelUp = new Signal<>();

    private ArrayList<DamageCalculator> damageScaling = new ArrayList<>();
    private ArrayList<DamageCalculator> damageResponse = new ArrayList<>();
    private ArrayList<Powerup> powerups = new ArrayList<>();

    public int getLevel() {
        return level;
    }

    public List<Powerup> getPowerups() {
        return Collections.unmodifiableList(powerups);
    }

    public Effect setLevel(int l) {
        level = l;
        return this;
    }

    public DamageInfo computeDamage(DamageInfo info) {
        DamageInfo inf = info;
        for (var scale : damageScaling) {
            inf = scale.compute(inf);
        }

        for (var power : powerups) {
            inf = power.outgoingDamageMod(inf);
        }

        return inf;
    }

    public DamageInfo computeDamageResistance(DamageInfo info) {
        if (info.isHealing()) {
            System.out.println("!!! > calculating healing in computeDamageResistance!!");
        }

        DamageInfo inf = info;
        for (var scale : damageResponse) {
            inf = scale.compute(inf);
        }

        for (var power : powerups) {
            inf = power.incomingDamageMod(inf);
        }

        return inf;
    }

    public Effect addDamageScaling(DamageCalculator scale) {
        damageScaling.add(scale);
        return this;
    }

    public Effect addDamageReceivingResponse(DamageCalculator scale) {
        damageResponse.add(scale);
        return this;
    }

    public Effect addDamageReceivingResponseExtra(Function<DamageInfo, DamageInfo> func) {
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

    public Effect levelUp() {
        level += 1;
        onLevelUp.emit(level);
        return this;
    }

    public <T extends Powerup> boolean hasPowerUpThenIncrementLevel(Class<T> clazz) {
        Optional<T> power = getPowerUp(clazz);

        power.ifPresent(p -> {
            p.levelUp();
        });

        return power.isPresent();
    }

    public Effect registerPowerup(Powerup powerup) {
        powerups.add(powerup);
        sortPowerups();
        return this;
    }

    public Effect unregisterPowerup(Powerup powerup) {
        powerups.remove(powerup);
        sortPowerups();
        return this;
    }

    private void sortPowerups() {
        powerups.sort((a, b) -> {
            return Integer.compare(b.getPriority(), a.getPriority());
        });
    }
}
