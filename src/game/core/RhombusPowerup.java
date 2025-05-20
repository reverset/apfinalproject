package game.core;

import java.time.Duration;

import com.raylib.Raylib;

import game.Color;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.Stopwatch;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class RhombusPowerup extends Powerup {

    private static RayTexture rhombus = new RayImage("resources/rhombus.png", 100, 100).uploadToGPU();
    private static Color tint = new Color(255, 255, 255, 100);
    private Transform trans;
    private Vec2 renderPosition = new Vec2();

    private static final int HEALING = 2;
    private static final Duration HEAL_INTERVAL = Duration.ofMillis(50);
    private static final Duration HEAL_START = Duration.ofSeconds(5);

    private int artificialHealth = 25;
    private int maxArtificialHealth = 25;

    private Health health;

    private Stopwatch healingStopwatch = Stopwatch.ofGameTime();
    private Stopwatch initialHealingStopwatch = Stopwatch.ofGameTime();

    private boolean isHealing = false;

    public RhombusPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
        setPriority(100);
    }

    @Override
    public void setup() {
        super.setup();
        trans = require(Transform.class);
        health = require(Health.class);
    }

    @Override
    public void ready() {
        super.ready();
        setMaxAHP();
        artificialHealth = maxArtificialHealth;
    }

    private void setMaxAHP() {
        maxArtificialHealth = (int) (health.getMaxHealth() * getPercentage());
    }

    private double getPercentage() {
        return 0.15 * level;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Richmond Rhombus";
    }

    @Override
    public String getDescription() {
        return "A shield providing\nartificial health";
    }

    @Override
    public void infrequentUpdate() {
        setMaxAHP();
        if (initialHealingStopwatch.hasElapsedAdvance(HEAL_START)) {
            isHealing = true;
        }
        
        if (isHealing && healingStopwatch.hasElapsedAdvance(HEAL_INTERVAL)) {
            artificialHealth = Math.min(maxArtificialHealth, artificialHealth + HEALING);
        }
    }

    @Override
    public DamageInfo incomingDamageMod(DamageInfo info) {
        if (info.isHealing()) return info;

        initialHealingStopwatch.restart();
        isHealing = false;
        
        if (artificialHealth > 0) {
            int dmg = info.damage();
            if (dmg > artificialHealth) dmg = artificialHealth;

            artificialHealth = Math.max(0, artificialHealth - info.damage());

            if (info.position().isPresent()) {
                GameLoop.safeTrack(DamageNumber.makeEntity(info.position().get(), dmg, health.getMaxHealth(), DamageColor.ARTIFICIAL));
            }
            return DamageInfo.ofNone().setVictim(entity);
        }

        return info;
    }

    @Override
    public void render() {
        renderPosition.setEq(trans.position.x - rhombus.width()/2, trans.position.y - rhombus.height()/2);
        if (artificialHealth == 0) return;
        
        rhombus.render(renderPosition, tint);
    }

    @Override
    public void hudRender() {
        Raylib.DrawText("+" + artificialHealth, 200, GameLoop.SCREEN_HEIGHT - 100, 54, Color.BLUE.getPointerNoUpdate());
    }

    @Override
    public String getSmallHUDInfo() {
        return "+" + maxArtificialHealth + " artifical health. (" + (int)(getPercentage()*100) + "%)";
    }

    @Override
    public String getIconPath() {
        return "resources/rhombusicon.png";
    }
    
}
