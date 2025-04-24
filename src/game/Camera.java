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

    private float shakeIntensity = 0;

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
        Raylib.Vector2 desiredPos = trans.position.asCanonicalVector2();
        if (shakeIntensity != 0) {
            float xRandom = (float) MoreMath.random(-1, 1);
            float yRandom = (float) MoreMath.random(-1, 1);
            desiredPos
                .x(desiredPos.x()+shakeIntensity*xRandom)
                .y(desiredPos.y()+shakeIntensity*yRandom);

            shakeIntensity -= delta()*10;
            shakeIntensity = Math.max(shakeIntensity, 0);
        }
        internal.target(desiredPos).offset(settings.offset.asCanonicalVector2()).zoom(settings.zoom).rotation(trans.rotation);
    }

    public void shake(float intensity) {
        shakeIntensity += intensity;
    }

    public Raylib.Camera2D getPointer() {
        return internal;
    }
}
