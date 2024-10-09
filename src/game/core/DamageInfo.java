package game.core;

import java.util.Optional;

import game.Vec2;

public record DamageInfo(int damage, Weapon2 weapon, Optional<Vec2> position, Optional<Object[]> extraInfo) {
    public DamageInfo(int damage, Weapon2 weapon, Vec2 position, Object[] extraInfo) {
        this(damage, weapon, Optional.of(position), Optional.of(extraInfo));
    }

    public DamageInfo(int damage, Weapon2 weapon, Vec2 position) {
        this(damage, weapon, Optional.of(position), Optional.empty());
    }

    public DamageInfo(int damage, Weapon2 weapon) {
        this(damage, weapon, Optional.empty(), Optional.empty());
    }

    public static DamageInfo ofNone() {
        return new DamageInfo(0, null);
    }

    public DamageInfo setExtras(Object[] extra) {
        return new DamageInfo(damage, weapon, position, Optional.of(extra));
    }


    public boolean hasExtra(Object obj) {
        if (extraInfo.isEmpty()) return false;

        Object[] inf = extraInfo.get();
        for (int i = 0; i < inf.length; i++) {
            if (obj == inf[i]) {
                return true;
            }
        }
        return false;
    }
}
