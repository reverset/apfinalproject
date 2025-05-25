package game.core;

import java.time.Duration;
import java.util.List;

import game.Color;
import game.EntityOf;
import game.RemoveAfter;
import game.Text;
import game.Tween;
import game.TweenAnimation;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class DamageNumber extends ECSystem {

    public static final Duration DURATION = Duration.ofSeconds(1);
    public static final float POP_DURATION = 0.1f;

    public static final int BASE_POPPED_FONT_SIZE = 54;

    private int value;
    private int maxHealth;
    private Color color;

    private Text text = new Text("", null, value, color);
    private Transform trans;

    private TweenAnimation animation;

    private Vec2 velocity;

    public DamageNumber(int value, Color color, int maxHealth) {
        this.value = value;
        this.color = color;
        this.maxHealth = maxHealth;

        velocity = Vec2.randomUnit().multiplyEq(100);
    }

    public static EntityOf<DamageNumber> makeEntity(Vec2 pos, int value, int maxHealth, Color color) {
        EntityOf<DamageNumber> entity = new EntityOf<>("DMG Num", DamageNumber.class);

        entity
            .addComponent(new Transform(pos))
            .register(new RemoveAfter(DURATION))
            .register(new DamageNumber(value, color, maxHealth));

        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        text.position = trans.position;
        
        text.color = color;
        text.text = String.format("%,d", value);
        
        int desiredPoppedSize = Math.min((int)(BASE_POPPED_FONT_SIZE + 300 * (value / (float)maxHealth)), 200);
        
        int orig = text.fontSize;
        text.fontSize = desiredPoppedSize;
        text.position.x -= text.measure()*0.5;
        text.fontSize = orig;

        animation = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(24, desiredPoppedSize), POP_DURATION, v -> {
                text.fontSize = v.intValue();
            }),
            new Tween<>(Tween.lerp(desiredPoppedSize, desiredPoppedSize*0.5f), POP_DURATION*0.75f, v -> {
                text.fontSize = v.intValue();
            })
        ));

        entity.setRenderPriority(120);

        entity.register(animation);
    }

    @Override
    public void ready() {
        animation.start();
    }

    @Override
    public void frame() {
        text.position.addEq(velocity.multiply(delta()));
    }
    
    @Override
    public void render() {
        final int SHIFT = 8;

        text.color = Color.BLACK; // kinda ugly but whatever
        text.position.x -= SHIFT/8;
        text.position.y -= SHIFT/2;
        text.fontSize += SHIFT;
        text.render();
        text.fontSize -= SHIFT;
        text.position.x += SHIFT/8;
        text.position.y += SHIFT/2;

        text.color = color;
        text.render();
    }
}
