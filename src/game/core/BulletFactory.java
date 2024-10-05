package game.core;

import java.time.Duration;
import java.util.ArrayList;

import game.Color;
import game.EntityOf;
import game.RemoveAfter;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.core.rendering.ViewCuller;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class BulletFactory {
    public static final int STANDARD_BULLET_SIZE = 10;
    public static final float STANDARD_BULLET_SPEED = 600;
    
    public static final Duration STANDARD_BULLET_LIFE = Duration.ofSeconds(3);

    public static final ArrayList<EntityOf<Bullet>> bullets = new ArrayList<>(400);

    public static EntityOf<Bullet> standardBullet(int damage, Transform trans, Vec2 direction, Color color, Entity owner, Object[] ignoreTags, Duration lifetime) {
        EntityOf<Bullet> entity = new EntityOf<>("Bullet", Bullet.class);
        
        entity
            .addComponent(trans.withPosition(trans.position.minus(STANDARD_BULLET_SIZE*0.5f)))
            .addComponent(() -> {
                var tangible = new Tangible();
                tangible.velocity = direction.multiply(STANDARD_BULLET_SPEED);
                return tangible;
            })
            .addComponent(new Rect(STANDARD_BULLET_SIZE, STANDARD_BULLET_SIZE, color))
            .register(new Physics(1, 0))
            .register(new RectRender())
            .register(new RemoveAfter(lifetime))
            .register(new ViewCuller(Vec2.screen().x+STANDARD_BULLET_SIZE))
            .register(new Bullet(owner, damage, ignoreTags));
        
        entity.onReady.listenOnce(v -> bullets.add(entity));
        entity.onDestroy.listenOnce(v -> bullets.remove(entity));
        
        return entity;
    }
}
