package game.core;

import java.time.Duration;

import game.GameLoop;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Diamond extends Powerup {

    public Diamond(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public DamageInfo outgoingDamageMod(DamageInfo info) {
        var vict = info.victim();
        vict.getComponent(Health.class).ifPresent(health -> {
            Transform victTrans = vict.getComponent(Transform.class).orElseThrow();

            GameLoop.runAfter(entity, Duration.ofMillis(500), () -> {
                health.damage(info.setPosition(victTrans.position.clone()).setColor(DamageColor.SPECIAL));
            });
        });

        return super.outgoingDamageMod(info);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void levelUp() {
        level += 1;
    }
    
}
