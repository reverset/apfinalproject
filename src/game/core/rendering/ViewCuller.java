package game.core.rendering;


import game.Camera;
import game.GameLoop;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class ViewCuller extends ECSystem {

    // Renderer renderer;
    Transform trans;
    float cullDistance;

    public ViewCuller(float cullDistance) {
        this.cullDistance = cullDistance;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        // renderer = requireSystem(Renderer.class);
    }
    
    @Override
    public void infrequentUpdate() {
        Camera cam = GameLoop.getMainCamera();
        float dist = trans.position.distance(cam.trans.position);
        if (!entity.isHidden() && dist >= cullDistance) {
            entity.hide();
        } else if (entity.isHidden() && dist < cullDistance) {
            entity.show();
        }
    }
}
