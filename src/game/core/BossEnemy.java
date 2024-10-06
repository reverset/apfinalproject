package game.core;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.PolyRenderer;
import game.core.rendering.Rect;
import game.ecs.comps.Transform;

public class BossEnemy extends Enemy {
    public static final float RADIUS = 50;
    public static final int BASE_HEALTH = 500;

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("Boss", Enemy.class);
        
        entity
            .addComponent(new Poly(6, RADIUS, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(Rect.around(RADIUS*2, Color.WHITE))
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Tangible())
            .addComponent(new Health(BASE_HEALTH))
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
        
    }
    
}
