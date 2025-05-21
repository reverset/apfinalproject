package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.MoreMath;
import game.RecoverableException;
import game.Signal;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Fluffy extends Unit {

    public final Signal<Boolean> onMiniModeChange = new Signal<>();

    private static final int MAX_HITS = 10;

    private Unit player;
    private int hits = 0;
    private TextureRenderer textureRenderer;
    private Rect rect;
    private Vec2 collisionOffset;
    private float desiredScale = 1;
    private float actualScale = 1;
    private Stopwatch resetStopwatch = Stopwatch.ofGameTime();
    private Stopwatch positionUpdate = Stopwatch.ofGameTime();
    private boolean isMiniMode = false;
    private Optional<Vec2> desiredPosition = Optional.empty();

    public static EntityOf<Fluffy> makeEntity(Entity player, int level) {
        EntityOf<Fluffy> e = new EntityOf<>("Fluffy", Fluffy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Tangible())
            .addComponent(new Health(100))
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Rect(150, 150, Color.WHITE))
            .register(new Physics(0, 0))
            .register(new TextureRenderer("resources/fluffy.png", 150, 150))
            .register(new Fluffy(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);

        return e;
    }

    public Fluffy(Entity player) {
        this.player = player.getSystem(Unit.class).orElseThrow(() -> new RecoverableException("Player is missing Unit system! (impossible)"));
    }

    @Override
    public void setup() {
        entity.setRenderPriority(70);
        getTransform().position = player.getTransform().position.clone();

        textureRenderer = requireSystem(TextureRenderer.class);

        getHealth().onDamage.listen(info -> {
            GameLoop.defer(() -> {
                getHealth().revive();
            });
            hit();
        }, entity);

        rect = require(Rect.class);

        collisionOffset = new Vec2(-rect.width/2, -rect.height/2);
        requireSystem(Physics.class).setHitboxOffset(collisionOffset);
    }
    
    @Override
    public void infrequentUpdate() {
        if (isMiniMode && resetStopwatch.hasElapsedAdvance(Duration.ofSeconds(5))) {
            setMiniMode(false);
        }
        if (positionUpdate.hasElapsedAdvance(Duration.ofMillis(500))) {
            desiredPosition = Optional.of(player.getTransform().position.addRandomByCoeff(50));
        }

        getHealth().setMaxHealthAndHealth(player.getHealth().getMaxHealth());

        actualScale = MoreMath.lerp(actualScale, desiredScale, 10 * infreqDelta());
        textureRenderer.setScale(actualScale);
    }

    private void setMiniMode(boolean on) {
        isMiniMode = on;
        if (isMiniMode) {
            desiredScale = 0.3f;
            resetStopwatch.restart();
        }
        else desiredScale = 1;
        getTangible().setTangible(!on);

        for (int i = 0; i < 2; i++) {
            GameLoop.safeTrack(HealingOrb.makeEntity(getTransform().position.clone(), getEffect().getLevel()*30));
        }

        onMiniModeChange.emit(isMiniMode);
    }

    @Override
    public void frame() {
        getTangible().velocity.setEq(0, 0);
        desiredPosition.ifPresent(p -> {
            getTransform().position.moveTowardsEq(p, 500 * delta());
        });
    }

    private void hit() {
        hits += 1;
        if (hits >= MAX_HITS) {
            setMiniMode(true);
            hits = 0;
        }
    }

    public void setLevel(int level) {
        getEffect().setLevel(level);
    }
}
