package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.GameLoop;
import game.RecoverableException;
import game.RemoveAfter;
import game.Tween;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Diamond extends Powerup { // Currently does not grant XP!
    private static final Duration DELAY = Duration.ofMillis(50);


    public Diamond(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public DamageInfo outgoingDamageMod(DamageInfo info) {
        if (Math.random() <= 0.1*level) {
            if (info.position().isEmpty()) return info;

            DamageInfo d = info
                .setDamage(info.damage()*4)
                .setColor(DamageColor.SPECIAL);

            Entity damageEffect = new Entity("Diamond Effect");

            X x = new X(d.position().get().clone().addRandomByCoeff(10), Color.AQUA, 8, 0);
            damageEffect
                .addComponent(x)
                .register(new Tween<>(Tween.overEase(0, 50, 20), 0.2f, val -> {
                    x.setLength(val);
                }).setDestroy(false).start())
                .register(new RemoveAfter(Duration.ofMillis(500)))
                .register(new XRenderer());
            
            GameLoop.safeTrack(damageEffect);
            GameLoop.safeTrack(HealingOrb.makeEntity(d.position().get().clone(), level*5));

            return d;
        }

        return info;
    }

    public int getWholePercentChance() {
        return (int) (0.1*level*100);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "D'amico Diamond";
    }

    @Override
    public String getDescription() {
        return "Occasionally quadruple\nyour damage.";
    }

    @Override
    public String getSmallHUDInfo() {
        return getWholePercentChance() + "% chance";
    }
    
    @Override
    public String getIconPath() {
        return "resources/diamond.png";
    }
}
