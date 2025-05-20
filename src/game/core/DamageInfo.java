package game.core;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import game.Color;
import game.Vec2;
import game.ecs.Entity;

// I only added the 'attacker' variable long after I made this class, idk why I didn't track it sooner.
public record DamageInfo(int damage, Entity victim, Weapon2 weapon, Optional<Vec2> position, Optional<Object[]> extraInfo, Color damageColor, Entity attacker, Consumer<DamageInfo> appListener) {
    public DamageInfo {
        if (damage < 0)
            damageColor = DamageColor.HEAL;
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position, Object[] extraInfo) {
        this(damage, victim, weapon, Optional.of(position), Optional.of(extraInfo), Color.WHITE, null, null);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position, Color color) {
        this(damage, victim, weapon, Optional.of(position), Optional.empty(), color, null, null);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon, Vec2 position) {
        this(damage, victim, weapon, Optional.of(position), Optional.empty(), Color.WHITE, null, null);
    }

    public DamageInfo(int damage, Entity victim, Weapon2 weapon) {
        this(damage, victim, weapon, Optional.empty(), Optional.empty(), Color.WHITE, null, null);
    }

    public static DamageInfo ofNone() {
        return new DamageInfo(0, null, null);
    }

    public DamageInfo setExtras(Object[] extra) {
        return new DamageInfo(damage, victim, weapon, position, Optional.of(extra), Color.WHITE, attacker, appListener);
    }

    public DamageInfo setColor(Color color) {
        return new DamageInfo(damage, victim, weapon, position, extraInfo, getColorPriority(color), attacker, appListener);
    }

    public DamageInfo setVictim(Entity victim) {
        return new DamageInfo(damage, victim, weapon, position, extraInfo, damageColor, attacker, appListener);
    }

    public DamageInfo clone() {
        var pos = position.map(p -> p.clone());
        return new DamageInfo(damage, victim, weapon, pos, extraInfo, damageColor, attacker, appListener);
    }

    public DamageInfo setPosition(Vec2 pos) {
        return new DamageInfo(damage, victim, weapon, Optional.ofNullable(pos), extraInfo, damageColor, attacker, appListener);
    }

    public DamageInfo setPosition(Optional<Vec2> pos) {
        return new DamageInfo(damage, victim, weapon, pos, extraInfo, damageColor, attacker, appListener);
    }

    public DamageInfo asHealing() {
        return new DamageInfo(damage > 0 ? -damage : damage, victim, weapon, position, extraInfo, damageColor, attacker, appListener);
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
        return new DamageInfo(dmg, victim, weapon, position, extraInfo, damageColor, attacker, appListener);
    }

    public DamageInfo setDamageAndColor(int dmg, Color color) {
        return new DamageInfo(dmg, victim, weapon, position, extraInfo, getColorPriority(color), attacker, appListener);
    }

    public DamageInfo setAttacker(Entity atk) {
        return new DamageInfo(damage, victim, weapon, position, extraInfo, damageColor, atk, appListener);
    }

    public DamageInfo setApplicationListener(Consumer<DamageInfo> list) {
        Consumer<DamageInfo> desired = appListener;
        if (desired == null) {
            desired = list;
        } else {
            desired = i -> {
                appListener.accept(i);
                list.accept(i);
            };
        }
        return new DamageInfo(damage, victim, weapon, position, extraInfo, damageColor, attacker, desired);
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

    public DamageInfo conditionalDamageMod(BooleanSupplier pred, Function<DamageInfo, Integer> calc) {
        if (pred.getAsBoolean()) {
            return this.setDamage(calc.apply(this));
        }
        return this;
    }
}
