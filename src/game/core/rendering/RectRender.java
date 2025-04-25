package game.core.rendering;

import game.Shader;
import game.Vec2;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import java.util.Optional;

import com.raylib.Raylib;

public class RectRender extends Renderer {
    private Rect rect;
    private Transform trans;
    private Optional<Shader> shader;
    private boolean hidden = false;
    private boolean hudMode = false;
    private boolean renderFromCenter = false;

    private static final Raylib.Rectangle rectCache = new Raylib.Rectangle();
    private static final Raylib.Vector2 centerCache = new Raylib.Vector2();

    @Override
    public void setup() {
        rect = require(Rect.class);
        trans = require(Transform.class);
        shader = optionallyRequire(Shader.class);
    }

    public RectRender setHudMode(boolean hudMode) {
        this.hudMode = hudMode;
        return this;
    }

    public RectRender centerize() {
        renderFromCenter = true;
        return this;
    }

    @Override
    public void render() {
        if (hudMode) return;
        if (hidden) return;

        if (shader.isPresent()) {
            Shader shade = shader.get();
            shade.activate();
            // Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
            drawRectangle();
            shade.deactivate();
        } else {
            drawRectangle();
            // Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
        }
    }

    private void drawRectangle() {
        rectCache
            .x(trans.position.xInt())
            .y(trans.position.yInt())
            .width(rect.width)
            .height(rect.height);
        
        if (renderFromCenter) {
            centerCache
                .x(rect.width / 2)
                .y(rect.height / 2);
        } else {
            centerCache.x(0).y(0);
        }
        
        Raylib.DrawRectanglePro(rectCache, centerCache, trans.rotation, rect.color.getPointer());
    }

    @Override
    public void hudRender() {
        if (!hudMode) return;
        if (hidden) return;

        if (shader.isPresent()) {
            Shader shade = shader.get();
            shade.activate();
            // Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
            drawRectangle();
            shade.deactivate();
        } else {
            drawRectangle();
            // Raylib.DrawRectangle(trans.position.xInt(), trans.position.yInt(), rect.width, rect.height, rect.color.getPointer());
        }
    }

    @Override
    public void hide() {
        hidden = true;
    }

    @Override
    public void show() {
        hidden = false;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }
    
}
