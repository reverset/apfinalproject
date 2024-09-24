package game;

import java.util.List;

import game.ecs.ECSystem;

public class TweenAnimation extends ECSystem {
    private List<Tween<?>> tweens;
    private int pointer = 0;

    public TweenAnimation(List<Tween<?>> tweens) { // Add simultaneous tweens.
        this.tweens = tweens;
        tweens.forEach(t -> {
            t.entity = entity;
            t.setDestroy(false);
        });
    }

    public void start(boolean restart) {
        if (restart) {
            var tween = tweens.get(pointer);
            tween.stop();
            tween.onFinish.unbind(entity);
            pointer = 0;
        } else if (pointer >= tweens.size()) {
            pointer = 0;
            return;
        }

        var tween = tweens.get(pointer);

        tween.onFinish.listenOnce(n -> {
            pointer += 1;
            start(false);
        });

        tween.start();
    }

    public void start() {
        start(true);
    }

    @Override
    public void frame() {
        tweens.get(pointer).frame();
    }

    @Override
    public void setup() {
    }
    
}
