package game;

import com.raylib.Raylib;

import game.core.rendering.Rect;

public class RayTexture {
    private Raylib.Texture internal;
    
    public RayTexture(Raylib.Texture internal) {
        this.internal = internal;
        Raylib.SetTextureFilter(internal, Raylib.TEXTURE_FILTER_POINT);

        Janitor.register(this, () -> {
            Raylib.UnloadTexture(internal);
            internal.close();
        });
    }

    public void render(Vec2 position, Color tint) {
        Raylib.DrawTextureV(internal, position.asCanonicalVector2(), tint.getPointer());
    }

    public void render(Vec2 position, float rotation, Color tint) {
        Raylib.DrawTextureEx(internal, position.asCanonicalVector2(), rotation, 1f, tint.getPointer());
    }

    public Rect getBoundingRectangle() {
        return new Rect(width(), height(), null);
    }

    public int width() {
        return internal.width();
    }

    public int height() {
        return internal.height();
    }
    
}
