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
        entity.setRenderPriority(-100);
    }

    
    @Override
    public void frame() {
    }

    @Override
    public void render() {
        Vec2 initial = GameLoop.getMainCamera().trans.position.minus(background.width()*2, background.height()*2).roundEq(1f / background.width(), 1f / background.height());
        
        Vec2 pos = new Vec2();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                pos.x = i*background.width();
                pos.y = j*background.height();
                background.render(pos.addEq(initial), Color.WHITE);
            }
        }

    }
    
}
