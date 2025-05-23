package game;


import com.raylib.Raylib;
import com.raylib.Raylib.Rectangle;

public class BetterButton extends Button {
    public final Signal<Void> onClick = new Signal<>();

    private Color outlineColor;
    private Color color;
    private int roundedness;
    private int segments;
    private Text text = new Text("", null, 0, Color.WHITE);
    private int outlineThickness = 8;

    private Tween<Vec2> hoverAnimation;
    private Tween<Vec2> unhoverAnimation;

    public BetterButton(Color outlineColor, Color color, int roundedness, int segments) {
        callback = () -> onClick.emit(null);
        this.outlineColor = outlineColor;
        this.color = color;
        this.roundedness = roundedness;
        this.segments = segments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setup() {
        super.setup();
        hoverAnimation = requireOrAddSystem(Tween.class, () -> {
            return GameLoop.makeTween(Tween.lerp(rect.dimensions(), rect.dimensions().multiply(2)), 1, val -> {
                rect.width = val.xInt();
                rect.height = val.yInt();
            }).setDestroy(false);
        });
        unhoverAnimation = hoverAnimation.reversedEntity();
        
    }

    public BetterButton setColor(Color color) {
        this.color = color;
        return this;
    }

    public BetterButton setOutlineThickness(int thickness) {
        outlineThickness = thickness;
        return this;
    }

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

    public BetterButton centerize() {
        GameLoop.defer(() -> { // not a fan of this
            trans.position.x -= rect.width/2;
            trans.position.y -= rect.height/2;
        });
        return this;
    }

    @Override
    public void hudRender() {
        // Vec2 mouse = GameLoop.getMouseScreenPosition();
        // if (rect.pointWithin(trans.position, mouse) && !hoverAnimation.isFinished()) {
        //     if (unhoverAnimation.isRunning()) unhoverAnimation.stop();
        //     if (!hoverAnimation.isRunning()) hoverAnimation.start();
        // } else if (hoverAnimation.isRunning() || hoverAnimation.isFinished()) {
        //     hoverAnimation.stop();
        //     if (!unhoverAnimation.isRunning()) unhoverAnimation.start();
        // }
        try (final Rectangle rectangle = new Rectangle()) {
            text.position = trans.position.add(rect.width/2, rect.height/2);
            Vec2 textDim = text.dimensions();
            text.position.x -= textDim.x/2;
            text.position.y -= textDim.y/2;

            rectangle.x(trans.position.x).y(trans.position.y).width(rect.width).height(rect.height);
            Raylib.DrawRectangleRounded(rectangle, roundedness, segments, color.getPointer());
            Raylib.DrawRectangleRoundedLines(rectangle, roundedness, segments, outlineThickness, outlineColor.getPointer());
            text.render();
        }

    }


}
