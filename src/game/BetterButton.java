package game;


import com.raylib.Raylib;
import com.raylib.Raylib.Rectangle;

import game.core.rendering.Rect;
import game.ecs.comps.Transform;

public class BetterButton extends Button {
    public final Signal<Void> onClick = new Signal<>();

    private Color outlineColor;
    private Color color;
    private int roundedness;
    private int segments;
    private Text text = new Text("", null, 0, Color.WHITE);

    public BetterButton(Color outlineColor, Color color, int roundedness, int segments) {
        callback = () -> onClick.emit(null);
        this.outlineColor = outlineColor;
        this.color = color;
        this.roundedness = roundedness;
        this.segments = segments;
    }

    // @Override
    // public void setup() {
    //     rect = require(Rect.class);
    //     trans = require(Transform.class);
    // }
    public BetterButton setText(String txt) {
        text.text = txt;
        return this;
    }

    public BetterButton setFontSize(int fontSize) {
        text.fontSize = fontSize;
        return this;
    }

    public BetterButton setTextColor(Color color) {
        text.color = color;
        return this;
    }

    @Override
    public void hudRender() {
        try (final Rectangle rectangle = new Rectangle()) {
            text.position = trans.position;
            rectangle.x(trans.position.x).y(trans.position.y).width(rect.width).height(rect.height);
            Raylib.DrawRectangleRounded(rectangle, roundedness, segments, color.getPointer());
            Raylib.DrawRectangleRoundedLines(rectangle, roundedness, segments, 8, outlineColor.getPointer());
            text.render();
        }
    }


}
