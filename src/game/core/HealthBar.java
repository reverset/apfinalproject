package game.core;

import java.util.Optional;

import game.Color;
import game.ImmutableColor;
import game.Text;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class HealthBar extends ECSystem {
    public static final int BAR_WIDTH = 100;
    public static final int BAR_HEIGHT = 10;

    public static final int BOSS_BAR_WIDTH = 500;
    public static final int BOSS_BAR_HEIGHT = 20;
    public static final int BOSS_BAR_Y_OFFSET = 30;

    public Vec2 offset;

    private Text message;
    private Text healthNums;

    private Health health;
    private Transform trans;
    private Optional<Effect> effect;
    
    private Rect healthBar = new Rect(BAR_WIDTH, BAR_HEIGHT, Color.RED.cloneIfImmutable());
    private Rect background = new Rect(BAR_WIDTH, BAR_HEIGHT, Color.GRAY);

    private Tween<float[]> colorTween = null;

    private boolean isBoss;

    public HealthBar(Vec2 offset, String message, boolean isBoss) {
        this.offset = offset;
        this.message = new Text(message, null, 18, Color.WHITE);
        healthNums = new Text("", null, 18, Color.WHITE);

        this.isBoss = isBoss;

        if (isBoss) {
            background.width = BOSS_BAR_WIDTH;
            background.height = BOSS_BAR_HEIGHT;

            healthBar.width = background.width;
            healthBar.height = background.height;
            this.message.fontSize = 22;
            healthNums.fontSize = 22;
        }
    }

    public HealthBar(Vec2 offset, String message) {
        this(offset, message, false);
    }

    @Override
    public void setup() {
        health = require(Health.class);
        trans = require(Transform.class);
        effect = optionallyRequire(Effect.class);

        effect.ifPresent(ef -> {
            message.text = "Lv" + ef.getLevel() + " " + message.text;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void ready() {
        colorTween = (Tween<float[]>) requireOrAddSystem(Tween.class, () ->  {
            return new Tween<>(Tween.lerp(255, 0), 0.2, val -> {
                healthBar.color.g = val.byteValue();
                healthBar.color.b = val.byteValue();
            }).setDestroy(false);
            // return new Tween<>(Tween.lerp(new float[]{255, 255, 255}, new float[]{255, 0, 0}), 0.2, val -> {
            //     healthBar.color = new ImmutableColor((int) val[0], (int) val[1], (int) val[2], 255);
            // }).setDestroy(false);
        });
        
        health.onDamage.listen(i -> colorTween.start());
    }

    @Override
    public void frame() {
        healthBar.width = (int) (health.getHealthPercentage()*background.width);
    }

    @Override
    public void render() {
        if (isBoss) {
            return;
        }

        Vec2 pos = trans.position.add(offset);
        background.renderRound(pos, 5, 5);
        healthBar.renderRound(pos, 5, 5);

        message.position = pos.addEq(0, -10);
        message.render();
    }

    @Override
    public void hudRender() {
        if (isBoss) bossBarRender();
    }

    public void bossBarRender() {
        Vec2 pos = Vec2.screenCenter();
        pos.y = BOSS_BAR_Y_OFFSET;
        pos.x -= background.width*0.5f;
        message.position = pos.clone();
        message.position.x += 15;
        
        healthNums.text = String.format("%,d/%,d", health.getHealth(), health.getMaxHealth());
        healthNums.position = pos.clone();
        healthNums.position.x += background.width - healthNums.measure() - 15;

        background.renderRound(pos, 5, 5);
        healthBar.renderRound(pos, 5, 5);

        message.render();
        healthNums.render();
    }
    
}
