package game.core;

import java.time.Duration;

import game.Color;
import game.GameLoop;
import game.RemoveAfter;
import game.Tween;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Diamond extends Powerup { // Currently does not grant XP!

    public Diamond(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public DamageInfo outgoingDamageMod(DamageInfo info) {
        var vict = info.victim();
        vict.getComponent(Health.class).ifPresent(health -> {
            Transform victTrans = vict.getComponent(Transform.class).orElseThrow();

            GameLoop.runAfter(entity, Duration.ofMillis(500), () -> {
                if (health.isAlive()) {
                    if (Math.random() > 0.1*level) return;

                    health.damageOrHeal(info.setPosition(victTrans.position.clone()).setColor(DamageColor.SPECIAL));
                    Entity damageEffect = new Entity("Diamond Effect");

                    X x = new X(victTrans.position.addRandomByCoeff(10), Color.RED, 8, 0);
                    damageEffect
                        .addComponent(x)
                        .register(new Tween<>(Tween.overEase(0, 50, 20), 0.2f, val -> {
                            x.setLength(val);
                        }).setDestroy(false).start())
                        .register(new RemoveAfter(Duration.ofMillis(500)))
                        .register(new XRenderer());
                    
                    GameLoop.safeTrack(damageEffect);
                }
            });
        });

        return super.outgoingDamageMod(info);
    }

    public int getWholePercentChance() {
        return (int) (0.1*level*100);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void levelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "D'amico Diamond";
    }

    @Override
    public String getDescription() {
        return "Chance to reapply damage.";
    }

    @Override
    public String getSmallHUDInfo() {
        return getWholePercentChance() + "%";
    }
    
}
