package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.RayImage;
import game.RayTexture;
import game.RecoverableException;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Blahaj extends ECSystem {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 52;

    private static final RayTexture TEXTURE = new RayImage("resources/blahaj.png", WIDTH, HEIGHT).uploadToGPU();

    private Transform trans;
    private Effect effect;

    private Optional<Vec2> desiredPosition = Optional.empty();

    private TextureRenderer textureRenderer;

    private Player player;

    public static EntityOf<Blahaj> makeEntity(Entity player, int level) {
        EntityOf<Blahaj> e = new EntityOf<>("Blahaj", Blahaj.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(WIDTH, HEIGHT, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .register(new TextureRenderer(TEXTURE).setFlipped(true))
            .register(new Physics(0, 0, new Vec2(-WIDTH/2, -HEIGHT/2)))
            .register(new AutoTeamRegister())
            .register(new Blahaj(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);

        return e;
    }

    public Blahaj(Entity player) {
        this.player = player.getSystem(Player.class).orElseThrow(() -> new RecoverableException("Player is missing player system!"));
    }

    public void setLevel(int level) {
        effect.setLevel(level);
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        effect = require(Effect.class);
        textureRenderer = requireSystem(TextureRenderer.class);

        entity.setRenderPriority(90);
    }

    @Override
    public void frame() {
        desiredPosition = Optional.of(player.getTransform().position);

        if (desiredPosition.isEmpty()) return;
        textureRenderer.setFlipped(trans.position.minus(desiredPosition.get()).x < 0);
    }
    
}
