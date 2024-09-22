package game;

import com.raylib.Raylib;

import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Camera extends ECSystem {
    public static Entity makeEntity(Transform transform, CameraSettings settings) {
        return new Entity("Camera")
            .addComponent(transform)
            .addComponent(settings)
            .register(new Camera());
    }

    private final Raylib.Camera2D internal;

    public Transform trans;
    public CameraSettings settings;

    public Camera() {
        internal = new Raylib.Camera2D();
        Janitor.register(this, internal::close);
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        settings = require(CameraSettings.class);
        updateCamera();
    }

    @Override
    public void frame() {
        updateCamera();
    }

    private void updateCamera() {
        internal.target(trans.position.getPointer()).offset(settings.offset.getPointer()).zoom(settings.zoom).rotation(trans.rotation);
    }

    public Raylib.Camera2D getPointer() {
        return internal;
    }
}
