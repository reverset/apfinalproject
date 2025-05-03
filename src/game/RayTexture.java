package game;

import com.raylib.Raylib;

import game.core.rendering.Rect;

public class RayTexture {
    private Raylib.Texture internal;

    private Raylib.Rectangle srcRect = null;
    private Raylib.Rectangle destRect = null;
    private Raylib.Vector2 origin = null;
    
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
        
        float x = position.x + width()/2f;
        float y = position.y + height()/2f;

        try {
            Raylib.rlPushMatrix();

            Raylib.rlTranslatef(x, y, 0); // in order to rotate around the center.
            Raylib.rlRotatef(rotation, 0, 0, -1);
            Raylib.rlTranslatef(-x, -y, 0);
            
            render(position, tint);
            // Raylib.DrawTextureEx(internal, position.asCanonicalVector2(), rotation, 1f, tint.getPointer());
        } finally {
            Raylib.rlPopMatrix();
        }
    }

    public void render(Vec2 position, float rotation, boolean hFlip, boolean vFlip, Color tint) {
        if (srcRect == null) srcRect = new Raylib.Rectangle();
        if (destRect == null) destRect = new Raylib.Rectangle();
        if (origin == null) origin = new Raylib.Vector2();

        srcRect.width(width() * (hFlip ? -1 : 1)).height(height());

        destRect.width(width()).height(height());
        destRect.x(position.x + width()/2).y(position.y + height()/2);

        origin
            .x(width()/2)
            .y(height()/2);

        // float x = position.x + width()/2f;
        // float y = position.y + height()/2f;

        Raylib.DrawTexturePro(internal, srcRect, destRect, origin, rotation, tint.getPointer());
        // try {
        //     Raylib.rlPushMatrix();

        //     Raylib.rlTranslatef(x, y, 0); // in order to rotate around the center.
        //     Raylib.rlRotatef(rotation, 0, 0, -1);
        //     Raylib.rlTranslatef(-x, -y, 0);
            
        // } finally {
        //     Raylib.rlPopMatrix();
        // }
    }

    public Rect getBoundingRectangle() {
        return new Rect(width(), height(), Color.WHITE);
    }

    public int width() {
        return internal.width();
    }

    public int height() {
        return internal.height();
    }
    
}
