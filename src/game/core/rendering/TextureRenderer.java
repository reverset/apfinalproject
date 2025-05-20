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
    private boolean enabled = true;
    private boolean flipped = false;
    private boolean hudMode = false;
    private float scale = 1;

    public TextureRenderer(String path) {
        this(path, -1, -1);
    }

    public TextureRenderer(String path, int width, int height) {
        this(new RayImage(path, width, height).uploadToGPU());
    }

    public TextureRenderer(RayTexture texture) {
        this.texture = texture;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public TextureRenderer setFlipped(boolean flipped) {
        this.flipped = flipped;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean val) {
        enabled = val;
    }

    public RayTexture getTexture() {
        return texture;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    public TextureRenderer setHudMode(boolean hud) {
        hudMode = hud;
        return this;
    }

    @Override
    public void render() {
        if (hudMode || !enabled) return;
        draw();
    }
    
    @Override
    public void hudRender() {
        if (!hudMode || !enabled) return;
        
        draw();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
    
    private void draw() {
        centeredPosition.x = trans.position.x - (texture.width()/2 * scale);
        centeredPosition.y = trans.position.y - (texture.height()/2 * scale);
        
        texture.render(centeredPosition, trans.rotation, flipped, false, scale, Color.WHITE);
    }
    
}
