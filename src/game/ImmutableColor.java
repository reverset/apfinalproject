package game;

import com.raylib.Raylib;

public class ImmutableColor extends Color { // idk why i even made this tbh
    public final byte r;
    public final byte g;
    public final byte b;
    public final byte a;

    public ImmutableColor(int r, int g, int b, int a) {
        super(r,g,b,a);
        this.r = (byte)r;
        this.g = (byte)g;
        this.b = (byte)b;
        this.a = (byte)a;
    }

    @Override
    public Raylib.Color getPointer() {
        return getPointerNoUpdate();
    }
}
