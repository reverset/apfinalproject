package game.core;

import java.time.Duration;

import game.Color;
import game.RemoveAfter;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class BulletFactory {
    public static final int STANDARD_BULLET_SIZE = 10;

    public static Entity standardBullet(Transform trans, Vec2 direction, float speed, Color color, Entity owner) {
        return new Entity("Bullet")
                .addComponent(trans.withPosition(trans.position.minus(STANDARD_BULLET_SIZE*0.5f)))
                .addComponent(() -> {
                    var tangible = new Tangible();
                    tangible.velocity = direction.multiply(speed);
                    return tangible;
                })
                .addComponent(new Rect(STANDARD_BULLET_SIZE, STANDARD_BULLET_SIZE, color))
                .register(new Physics())
                .register(new RectRender())
                .register(new RemoveAfter(Duration.ofSeconds(8)))
                .register(new Bullet(owner));
    }
}
