package game.core;

import game.Color;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.Entity;

public class Background extends ECSystem {

    private RayTexture background = new RayImage("resources/stars.jpg").uploadToGPU();

    public static Entity makeEntity() {
        return new Entity("Background")
            .register(new Background());
    }

    @Override
    public void setup() {
    }

    
    @Override
    public void frame() {
    }

    @Override
    public void render() {
        Vec2 initial = GameLoop.getMainCamera().trans.position.minus(background.width()*2, background.height()*2).roundEq(1f / background.width(), 1f / background.height());
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                background.render(initial.add(new Vec2(i*background.width(), j*background.height())), Color.WHITE);
            }
        }

    }
    
}
