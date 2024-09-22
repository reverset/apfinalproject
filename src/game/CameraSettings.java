package game;

import game.ecs.Component;

public class CameraSettings implements Component {
    public Vec2 offset;
    public float zoom;

    public CameraSettings(Vec2 offset, float zoom) {
        this.offset = offset;
        this.zoom = zoom;
    }
}
