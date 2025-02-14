package game.core;

import java.util.function.Supplier;

import game.ecs.ECSystem;

public class ExpAccumulator extends ECSystem {

    private Effect effect;
    private int xp = 0;
    private final int maxXp;
    private Supplier<Integer> xpSupplier = null;

    public ExpAccumulator(int maxXp) {
        this.maxXp = maxXp;
    }

    public ExpAccumulator addXpSupplier(Supplier<Integer> xp) {
        if (xpSupplier == null) {
            xpSupplier = xp;
        } else {
            var original = xpSupplier;
            xpSupplier = () -> {
                var prev = original.get();
                if (prev == null) return xp.get();
                return prev + xp.get();
            };
        }
        return this;
    }


    @Override
    public void setup() {
        effect = require(Effect.class);
    }

    @Override
    public void infrequentUpdate() {
        if (xpSupplier == null) return;
        Integer newXp = xpSupplier.get();
        if (newXp == null) return;

        accumulate(newXp.intValue());
    }

    public ExpAccumulator accumulate(int xp) {
        if (xp <= 0) return this;
        this.xp += xp;
        if (this.xp >= maxXp) {
            int remaining = this.xp - maxXp;
            this.xp = 0;
            effect.levelUp();
            return accumulate(remaining);
        }
        System.out.println("XP Reward of: " + xp);
        System.out.println(this.xp + " / " + maxXp);
        System.out.println("Level: " + effect.getLevel());
        return this;
    }
    
}
