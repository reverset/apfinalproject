package game;

import game.core.DamageInfo;
import game.core.Effect;
import game.core.HealingOrb;
import game.core.Health;
import game.core.Powerup;
import game.core.Weapon2;
import game.ecs.Entity;

public class HealthSyphon extends Powerup {


    public HealthSyphon(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void levelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Health Syphon";
    }

    @Override
    public String getDescription() {
        return "Heal a percentage of an\nenemy's max health.";
    }

    @Override
    public DamageInfo outgoingDamageMod(DamageInfo info) {
        info.victim().getComponent(Health.class).ifPresent(health -> {
            GameLoop.defer(() -> {
                if (health.isAlive()) return;
                
                int healing = (int) (health.getMaxHealth() * getPercent());
                info.position().ifPresent(pos -> {
                    GameLoop.safeTrack(HealingOrb.makeEntity(pos.clone(), healing));
                });
            });
        });


        return super.outgoingDamageMod(info);
    }

    @Override
    public String getSmallHUDInfo() {
        return ((int)(100 * getPercent())) + "%";
    }

    private double getPercent() {
        return 0.25 * level;
    }
    
}
