package game;

import java.time.Duration;

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
    private final Stopwatch shakeIntervals = Stopwatch.ofGameTime();

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
        final Duration shakeDur = Duration.ofMillis(16);
        if (shakeIntensity != 0 && shakeIntervals.hasElapsedAdvance(shakeDur)) {
            float xRandom = (float) MoreMath.random(-1, 1);
            float yRandom = (float) MoreMath.random(-1, 1);
            desiredPos
                .x(desiredPos.x()+shakeIntensity*xRandom)
                .y(desiredPos.y()+shakeIntensity*yRandom);

            shakeIntensity -= (shakeDur.toMillis() / 1_000.0 )*10;
            shakeIntensity = Math.max(shakeIntensity, 0);
        }
        internal.target(desiredPos).offset(settings.offset.asCanonicalVector2()).zoom(settings.zoom).rotation(trans.rotation);
    }

    public void shake(float intensity) {
        shakeIntensity += intensity;
        shakeIntervals.restart();
    }

    public Raylib.Camera2D getPointer() {
        return internal;
    }
}
