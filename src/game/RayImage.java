package game;

import com.raylib.Raylib;

public class RayImage {
    private Raylib.Image internal;
    private int width;
    private int height;

    private Color[][] pixels = null;

    public RayImage(Raylib.Image internal) {
        this.internal = internal;
        width = internal.width();
        height = internal.height();

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

    public RayImage(String path) {
        this(Raylib.LoadImage(path));
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
