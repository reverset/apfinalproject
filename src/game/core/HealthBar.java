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

    public Vec2 offset;
    
    private Text message;

    private Health health;
    private Transform trans;
    private Optional<Effect> effect;
    
    private Rect healthBar = new Rect(100, 10, Color.RED);
    private Rect background = new Rect(100, 10, Color.GRAY);

    private Tween<float[]> colorTween = null;

    public HealthBar(Vec2 offset, String message) {
        this.offset = offset;
        this.message = new Text(message, null, 18, Color.WHITE);
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
            return new Tween<>(Tween.lerp(new float[]{255, 255, 255}, new float[]{255, 0, 0}), 0.2, val -> {
                healthBar.color = new ImmutableColor((int) val[0], (int) val[1], (int) val[2], 255);
            }).setDestroy(false);
        });
        
        health.onDamage.listen(i -> colorTween.start());
        
        // Stopwatch stop = new Stopwatch();
        // health.onDamage.listen((i) -> {
        //     schedule(List.of(() -> {
        //         healthBar.color = Color.WHITE;
        //         stop.restart();
        //         return true;
        //     }, () -> stop.hasElapsedSeconds(0.1), 
        //     () -> {
        //         healthBar.color = Color.RED;
        //         return true;
        //     }));
        // });
    }

    @Override
    public void frame() {
        healthBar.width = (int) (health.getHealthPercentage()*100);
    }

    @Override
    public void render() {
        Vec2 pos = trans.position.add(offset);
        background.renderRound(pos, 5, 5);
        healthBar.renderRound(pos, 5, 5);

        message.position = pos.addEq(0, -10);
        message.render();
    }
    
}
