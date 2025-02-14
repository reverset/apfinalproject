package game;

import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class LoadingBar extends ECSystem {

    private Transform trans;
    private Rect rect;
    private Vec2 desiredDimensions;
    private FloatSupplier supplier;

    private float maxValue;
    private float currentValue = 0;

    public LoadingBar(FloatSupplier supp, float maxValue) {
        this.maxValue = maxValue;
        supplier = supp;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        rect = require(Rect.class);

        desiredDimensions = rect.dimensions().clone();
        rect.width = 0;
    }
    
    
    @Override
    public void hudRender() {
        rect.width = desiredDimensions.xInt();
        rect.renderWithColor(trans.position, Color.GRAY);

        currentValue = supplier.getAsFloat();
        rect.width = (int) (desiredDimensions.x * getPercentage());

        rect.render(trans.position);
    }

    public float getPercentage() {
        return currentValue / maxValue;
    }

    public float getProgress() {
        return currentValue;
    }

    public float getMaxProgress() {
        return maxValue;
    }
}
