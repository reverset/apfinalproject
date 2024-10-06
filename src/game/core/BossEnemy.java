package game.core;

import java.util.List;
import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Shader;
import game.ShaderUpdater;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
import game.core.rendering.Rect;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class BossEnemy extends Enemy {
    public static final float RADIUS = 50;
    public static final int SIDES = 6;
    public static final int BASE_HEALTH = 500;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("Boss", Enemy.class);

        Supplier<Float> timeSupplier = ECSystem::time; // ????
        entity
            .addComponent(new Shader("resources/enemy.frag"))
            .addComponent(new Poly(6, RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(Rect.around(RADIUS*2, Color.WHITE))
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH))
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new HealthBar(new Vec2(-RADIUS, -100), entity.name, true))
            .register(new Physics(0, 0, new Vec2(-RADIUS, -RADIUS)))
            .register(new PolyRenderer())
            .register(new BossEnemy())
            .addTags(GameTags.ENEMY, GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);

        Entity last = entity;
        for (int i = 1; i < 15; i++) {
            final int j = i;
            Transform t = last.getComponent(Transform.class).orElseThrow();
            EntityOf<BossBody> body = BossBody.makeEntity(() -> t.position, () -> tangible.velocity.normalize().multiplyEq(j*4));
            GameLoop.safeTrack(body);
            last = body;
        }
    }

    @Override
    public void ready() {
        player = GameLoop.findEntityByTag(GameTags.PLAYER);
        player.ifPresent(p -> {
            playerTransform = p.getComponent(Transform.class).orElseThrow();
        });
    }

    @Override
    public void frame() {
        if (playerTransform == null) return;
        
        tangible.velocity = Vec2.fromAngle(time()).multiplyEq(200);
    }
    
}
