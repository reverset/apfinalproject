package game;

import com.raylib.Raylib;

public class RayImage {
    private Raylib.Image internal;
    private int width;
    private int height;

    private Color[][] pixels = null;

    public RayImage(Raylib.Image internal, int width, int height) {
        this.internal = internal;
        width = width == -1 ? internal.width() : width;
        height = height == -1 ? internal.height() : height;

        Raylib.ImageResizeNN(internal, width, height);
        
        pixels = new Color[width][height];
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Raylib.Color col = Raylib.GetImageColor(internal, i, j);
                pixels[i][j] = new Color(col);
            }
        }

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

    public Color colorAt(Vec2 pos) {
        return pixels[pos.xInt()][pos.yInt()];
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
