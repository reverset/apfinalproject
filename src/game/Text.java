package game;

import com.raylib.Raylib;

public class Text {
    public Vec2 position;
    public int fontSize;
    public String text;
    public Color color;

    public Text(String text, Vec2 position, int fontSize, Color color) {
        this.position = position;
        this.fontSize = fontSize;
        this.text = text;
        this.color = color;
    }

    public void render() {
        Raylib.DrawText(text, position.xInt(), position.yInt(), fontSize, color.getPointer());
    }

    public int measure() {
        return Raylib.MeasureText(text, fontSize);
    }

    public Text center() {
        position.x -= measure()*0.5f;
        return this;
    }
}
