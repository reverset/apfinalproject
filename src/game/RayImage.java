package game;

import com.raylib.Raylib;

public class RayImage {
    private Raylib.Image internal;
    private int width;
    private int height;

    public RayImage(Raylib.Image internal, int width, int height) {
        this.internal = internal;
        width = width == -1 ? internal.width() : width;
        height = height == -1 ? internal.height() : height;

        Raylib.ImageResizeNN(internal, width, height);
        
        Janitor.register(this, () -> {
            Raylib.UnloadImage(internal);
            internal.close();
        });

    }

    public RayImage(Raylib.Image internal) {
        this(internal, -1, -1);
    }

    public RayImage(String path) {
        this(path, -1, -1);
    }

    public RayImage(String path, int width, int height) {
        this(Raylib.LoadImage(path), width, height);
    }

    public static RayImage perlinNoise(Vec2 dimensions, Vec2 offset, float scale) {
        return new RayImage(
            Raylib.GenImagePerlinNoise(
                dimensions.xInt(), dimensions.yInt(), offset.xInt(), offset.yInt(), scale));
    }

    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    public RayTexture uploadToGPU() {
        return new RayTexture(Raylib.LoadTextureFromImage(internal));
    }
    
    public Vec2 dimensions() {
        return new Vec2(width, height);
    }

    public Raylib.Image getPointer() {
        return internal;
    }
}
