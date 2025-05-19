package game.core;

import game.Color;
import game.EntityOf;
import game.RecoverableException;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Fluffy extends Unit { // TODO

    private Unit player;
    private int hits = 0;
    private TextureRenderer textureRenderer;
    private Rect rect;
    private Vec2 collisionOffset;

    public static EntityOf<Fluffy> makeEntity(Entity player, int level) {
        EntityOf<Fluffy> e = new EntityOf<>("Fluffy", Fluffy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Tangible())
            .addComponent(new Health(Integer.MAX_VALUE))
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Rect(50, 50, Color.WHITE))
            .register(new Physics(0, 0))
            .register(new TextureRenderer("resources/fluffy.png"))
            .register(new Fluffy(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);

        return e;
    }

    public Fluffy(Entity player) {
        this.player = player.getSystem(Unit.class).orElseThrow(() -> new RecoverableException("Player is missing Unit system! (impossible)"));
    }

    @Override
    public void setup() {
        getTransform().position = player.getTransform().position.clone();

        textureRenderer = requireSystem(TextureRenderer.class);

        getHealth().onDamage.listen(info -> {
            getHealth().setHealth(Integer.MAX_VALUE);
            hit();
        }, entity);

        rect = require(Rect.class);

        collisionOffset = new Vec2(-rect.width/2, -rect.height/2);
        requireSystem(Physics.class).setHitboxOffset(collisionOffset);
    }
    
    @Override
    public void frame() {
        getTangible().velocity.setEq(0, 0);
        textureRenderer.setScale(hits+1);
    }

    private void hit() {
        hits += 1;
        rect.width *= 1.5;
        rect.height *= 1.5;
        collisionOffset.x = -rect.width/2;
        collisionOffset.y = -rect.height/2;   
    }
}
