package game.core;

import java.util.Optional;

import game.Color;
import game.Vec2;
import game.ecs.Entity;

public record DamageInfo(int damage, Entity victim, Weapon2 weapon, Optional<Vec2> position, Optional<Object[]> extraInfo, Color damageColor) {
    public DamageInfo {
        if (damage < 0)
            damageColor = DamageColor.HEAL;
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position, Object[] extraInfo) {
        this(damage, victim, weapon, Optional.of(position), Optional.of(extraInfo), Color.WHITE);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position, Color color) {
        this(damage, victim, weapon, Optional.of(position), Optional.empty(), color);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position) {
        this(damage, victim, weapon, Optional.of(position), Optional.empty(), Color.WHITE);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon) {
        this(damage, victim, weapon, Optional.empty(), Optional.empty(), Color.WHITE);
    }

    public static DamageInfo ofNone() {
        return new DamageInfo(0, null, null);
    }

    public DamageInfo setExtras(Object[] extra) {
        return new DamageInfo(damage, victim, weapon, position, Optional.of(extra), Color.WHITE);
    }

    public DamageInfo setColor(Color color) {
        return new DamageInfo(damage, victim, weapon, position, extraInfo, getColorPriority(color));
    }

    public DamageInfo setVictim(Entity victim) {
        return new DamageInfo(damage, victim, weapon, position, extraInfo, damageColor);
    }

    public DamageInfo clone() {
        var pos = position.map(p -> p.clone());
        return new DamageInfo(damage, victim, weapon, pos, extraInfo, damageColor);
    }

    public DamageInfo setPosition(Vec2 pos) {
        return new DamageInfo(damage, victim, weapon, Optional.ofNullable(pos), extraInfo, damageColor);
    }

    public DamageInfo setPosition(Optional<Vec2> pos) {
        return new DamageInfo(damage, victim, weapon, pos, extraInfo, damageColor);
    }

    public DamageInfo asHealing() {
        return new DamageInfo(damage > 0 ? -damage : damage, victim, weapon, position, extraInfo, damageColor);
    }

    public Color getColorPriority(Color color) {
        if (color.equals(DamageColor.SPECIAL)) {
            return color;
        } else if (damageColor.equals(DamageColor.SPECIAL)) {
            return damageColor;
        } else if (damageColor.equals(DamageColor.CRITICAL)) {
            return damageColor;
        } else if (color.equals(DamageColor.CRITICAL)) {
            return color;
        } else if (damageColor.equals(DamageColor.NORMAL)) {
            return color;
        }

        return damageColor;
    }

    public DamageInfo setDamage(int dmg) {
        return new DamageInfo(dmg, victim, weapon, position, extraInfo, damageColor);
    }

    public DamageInfo setDamageAndColor(int dmg, Color color) {
        return new DamageInfo(dmg, victim, weapon, position, extraInfo, getColorPriority(color));
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

    public boolean isHealing() {
        return damage < 0;
    }

    public boolean isHarmful() {
        return !isHealing();
    }

    public int absoluteDamageOrHeal() {
        return Math.abs(damage);
    }
}
