package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RemoveAfter;
import game.Shader;
import game.ShaderUpdater;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
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

    public static EntityOf<Bullet> bullet(int damage, Optional<Effect> effect, Vec2 pos, Vec2 velocity, Color color, Entity owner, Object[] ignoreTags, Duration lifetime, Weapon2 weapon) {
        EntityOf<Bullet> entity = new EntityOf<>("Bullet", Bullet.class);

        entity
            .addComponent(new Transform(pos).withPosition(pos.minus(STANDARD_BULLET_SIZE*0.5f)))
            .addComponent(() -> {
                var tangible = new Tangible();
                tangible.velocity = velocity;
                return tangible;
            })
            .addComponent(new Rect(STANDARD_BULLET_SIZE, STANDARD_BULLET_SIZE, color))
            .register(new Physics(1, 0))
            .register(new RectRender())
            .register(new RemoveAfter(lifetime))
            .register(new ViewCuller(Vec2.screen().x+STANDARD_BULLET_SIZE))
            .register(new Bullet(owner, damage, effect, ignoreTags, weapon));
        
        entity.onReady.listenOnce(v -> bullets.add(entity));
        entity.onDestroy.listenOnce(v -> bullets.remove(entity));

        return entity;
    }

    public static EntityOf<HexaBomb> hexaBomb(int damagePerPellet, Optional<Effect> effect, Vec2 pos, Vec2 velocity, Color color, Entity owner, Object[] ignoreTags, Duration lifetime, Weapon2 weapon) {
        EntityOf<HexaBomb> entity = new EntityOf<>("hexabomb", HexaBomb.class);

        Supplier<Float> timeSupplier = GameLoop::getTime;
        entity
            .addComponent(new Transform(pos))
            .addComponent(Shader.fromCacheOrLoad("resources/hexabomb.frag")) // update shader to use color given.
            .addComponent(() -> {
                var tangible = new Tangible();
                tangible.velocity = velocity;
                return tangible;
            })
            .addComponent(Rect.around(HexaBomb.RADIUS*2, Color.WHITE))
            .addComponent(new Poly(6, HexaBomb.RADIUS, color))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new PolyRenderer())
            .register(new Physics(1, 0))
            .register(new HexaBomb(lifetime, effect, owner, damagePerPellet, ignoreTags, color, weapon));

        return entity;
    }
}
