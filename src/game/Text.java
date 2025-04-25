package game;

import com.raylib.Raylib;

import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

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
        this.text = this.text.replace("\t", "    ");
    }

    public static Entity makeEntity(Text text) {
        return new Entity("entity::" + text.text)
            .register(new ECSystem() {
                private Text internal = null;

                @Override
                public void setup() {
                    internal = text;
                }
                
                @Override
                public void hudRender() {
                    internal.renderWithNewlines();
                }
            });
    }

    public static Entity makeEntity(String text, Vec2 position, int fontSize, Color color) {
        return new Entity("entity::"+text)
            .register(new ECSystem() {
                private Text internal = null;

                @Override
                public void setup() {
                    internal = new Text(text, position, fontSize, color);
                }
                
                @Override
                public void hudRender() {
                    internal.renderWithNewlines();
                }
            }
        );
    }

    public void render() {
        Raylib.DrawText(text, position.xInt(), position.yInt(), fontSize, color.getPointer());
    }

    public void renderWithNewlines() {
        String[] txts = text.split("\n");
        for (int i = 0; i < txts.length; i++) {
            var t = txts[i];
            Raylib.DrawText(t, position.xInt(), position.yInt() + (i*fontSize), fontSize, color.getPointer());
        }
    }

    public int measure() {
        return Raylib.MeasureText(text, fontSize);
    }

    public Vec2 dimensions() {
        return new Vec2(Raylib.MeasureTextEx(Raylib.GetFontDefault(), text, fontSize, 2));
    }

    public Text center() {
        position.x -= measure()*0.5f;
        return this;
    }
}
