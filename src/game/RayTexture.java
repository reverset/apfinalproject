package game;

import com.raylib.Raylib;

public class RayTexture {
    private Raylib.Texture internal;
    
    public RayTexture(Raylib.Texture internal) {
        this.internal = internal;

        Janitor.register(this, () -> {
            Raylib.UnloadTexture(internal);
            internal.close();
        });
    }

    public void render(Vec2 position, Color tint) {
        Raylib.DrawTextureV(internal, position.getPointer(), tint.getPointer());
    }

    public int width() {
        return internal.width();
    }

    public int height() {
        return internal.height();
    }
    
}
