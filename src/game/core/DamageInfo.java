package game.core;

import java.util.Optional;

import game.Color;
import game.Vec2;

public record DamageInfo(int damage, Weapon2 weapon, Optional<Vec2> position, Optional<Object[]> extraInfo, Color damageColor) {
    public DamageInfo(int damage, Weapon2 weapon, Vec2 position, Object[] extraInfo) {
        this(damage, weapon, Optional.of(position), Optional.of(extraInfo), Color.WHITE);
    }

    public DamageInfo(int damage, Weapon2 weapon, Vec2 position, Color color) {
        this(damage, weapon, Optional.of(position), Optional.empty(), color);
    }

    public DamageInfo(int damage, Weapon2 weapon, Vec2 position) {
        this(damage, weapon, Optional.of(position), Optional.empty(), Color.WHITE);
    }

    public DamageInfo(int damage, Weapon2 weapon) {
        this(damage, weapon, Optional.empty(), Optional.empty(), Color.WHITE);
    }

    public static DamageInfo ofNone() {
        return new DamageInfo(0, null);
    }

    public DamageInfo setExtras(Object[] extra) {
        return new DamageInfo(damage, weapon, position, Optional.of(extra), Color.WHITE);
    }

    public DamageInfo setColor(Color color) {
        return new DamageInfo(damage, weapon, position, extraInfo, color);
    }

    public DamageInfo setDamage(int dmg) {
        return new DamageInfo(dmg, weapon, position, extraInfo, damageColor);
    }

    public DamageInfo setDamageAndColor(int dmg, Color color) {
        return new DamageInfo(dmg, weapon, position, extraInfo, color);
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
