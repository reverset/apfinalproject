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

    int value;
    Color color;

    Text text = new Text("", null, value, color);
    Transform trans;

    TweenAnimation animation;

    public DamageNumber(int value, Color color) {
        this.value = value;
        this.color = color;
    }

    public static EntityOf<DamageNumber> makeEntity(Vec2 pos, int value, Color color) {
        EntityOf<DamageNumber> entity = new EntityOf<>("DMG Num", DamageNumber.class);

        entity
            .addComponent(new Transform(pos))
            .register(new RemoveAfter(DURATION))
            .register(new DamageNumber(value, color));

        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        text.position = trans.position;
        text.color = color;
        text.text = String.valueOf(value);

        int desiredPoppedSize = BASE_POPPED_FONT_SIZE + value*2;
        animation = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(24, desiredPoppedSize), POP_DURATION, v -> {
                text.fontSize = v.intValue();
            }),
            new Tween<>(Tween.lerp(desiredPoppedSize, desiredPoppedSize*0.5f), POP_DURATION*0.75f, v -> {
                text.fontSize = v.intValue();
            })
        ));

        entity.register(animation);
    }

    @Override
    public void ready() {
        animation.start();
    }

    @Override
    public void frame() {
        text.position.y -= 50 * delta();
    }
    
    @Override
    public void render() {
        final int SHIFT = 4;

        text.color = Color.BLACK;
        text.position.x -= SHIFT/2;
        text.fontSize += SHIFT;
        text.render();
        text.fontSize -= SHIFT;
        text.position.x += SHIFT/2;

        text.color = color;
        text.render();
    }
}
