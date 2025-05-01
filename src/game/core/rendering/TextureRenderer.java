package game.core.rendering;

import com.raylib.Raylib;

import game.Color;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class TextureRenderer extends ECSystem {

    private RayTexture texture;
    private Transform trans;
    private Vec2 centeredPosition = new Vec2();

    public TextureRenderer(String path) {
        this(new RayImage(path).uploadToGPU());
    }

    public TextureRenderer(RayTexture texture) {
        this.texture = texture;
    }

    public RayTexture getTexture() {
        return texture;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public void render() {
        centeredPosition.x = trans.position.x - texture.width()/2;
        centeredPosition.y = trans.position.y - texture.height()/2;
        texture.render(centeredPosition, trans.rotation, Color.WHITE);
    }
    
}
