package game;

import com.raylib.Raylib;

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

    public int width() {
        return internal.width();
    }

    public int height() {
        return internal.height();
    }
    
}
