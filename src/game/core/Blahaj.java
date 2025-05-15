package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.RecoverableException;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Blahaj extends Unit {

    public enum State {
        FOLLOWING,
        HEALING,
        ATTACKING
    }

    private static final int WIDTH = 100;
    private static final int HEIGHT = 52;
    private static final int MAX_SPEED = 300;
    private static final float BITE_DISTANCE = 10;
    private static final int BASE_DAMAGE = 30;

    private static final RayTexture TEXTURE = new RayImage("resources/blahaj.png", WIDTH, HEIGHT).uploadToGPU();

    private Optional<Vec2> desiredPosition = Optional.empty();

    private TextureRenderer textureRenderer;

    private Player player;
    private Optional<Unit> target = Optional.empty();
    private State state = State.FOLLOWING;

    private Stopwatch biteStopwatch = Stopwatch.ofGameTime();
    private Stopwatch healStopwatch = Stopwatch.ofGameTime();

    private Stopwatch flipStopwatch = Stopwatch.ofGameTime();

    public static EntityOf<Blahaj> makeEntity(Entity player, int level) {
        EntityOf<Blahaj> e = new EntityOf<>("Blahaj", Blahaj.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(WIDTH, HEIGHT, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .addComponent(Health.ofInvincible())
            .register(new TextureRenderer(TEXTURE).setFlipped(true))
            .register(new Physics(0, 0, new Vec2(-WIDTH/2, -HEIGHT/2)))
            .register(new Blahaj(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);

        return e;
    }

    public Blahaj(Entity player) {
        this.player = player.getSystem(Player.class).orElseThrow(() -> new RecoverableException("Player is missing player system!"));
    }

    public State getState() {
        return state;
    }

    public void setAttacking() {
        state = State.ATTACKING;
    }

    public void attack(Unit target) {
        this.target = Optional.of(target);
        setAttacking();
    }

    public void setHealing() {
        state = State.HEALING;
        target = Optional.empty();
    }

    public void setFollowing() {
        state = State.FOLLOWING;
        target = Optional.empty();
    }

    public void setLevel(int level) {
        getEffect().setLevel(level);
    }

    @Override
    public void setup() {
        textureRenderer = requireSystem(TextureRenderer.class);
        getTransform().position = player.getTransform().position.clone();

        getEffect().addDamageScaling(info -> info.damage() * getEffect().getLevel());

        entity.setRenderPriority(90);
    }

    @Override
    public void frame() {
        if (desiredPosition.isEmpty()) return;

        if (flipStopwatch.hasElapsedAdvance(Duration.ofMillis(100))) {
            textureRenderer.setFlipped(getTransform().position.minus(desiredPosition.get()).x < 0);
        }


        getTransform().rotation = (getTangible().velocity.y / MAX_SPEED) * 12;
        getTransform().rotation *= textureRenderer.isFlipped() ? 1 : -1;
    }

    @Override
    public void infrequentUpdate() {
        if (player.getHealth().isCritical()) {
            setHealing();
        } else if (target.isEmpty()) {
            target = getTeam().findTarget(getTransform().position);
            if (target.isPresent()) {
                setAttacking();
            }
        } else if (target.isPresent() && !getTeam().shouldEntityBeTargetted(target.get().getEntity())) setFollowing();
        else if (target.isPresent() && target.get().getTransform().position.distance(player.getTransform().position) > 1_000) setFollowing();

        switch (state) {
            case FOLLOWING, HEALING -> {
                desiredPosition = Optional.of(getDesiredPlayerFollowPosition());

                int hp = 10 * getEffect().getLevel();
                if (state == State.HEALING) {
                    if (healStopwatch.hasElapsedAdvance(Duration.ofMillis(400))) {
                        GameLoop.safeTrack(HealingOrb.makeEntity(getTransform().position.clone(), hp));
                    }
                } else {
                    if (healStopwatch.hasElapsedAdvance(Duration.ofSeconds(4))) {
                        GameLoop.safeTrack(HealingOrb.makeEntity(getTransform().position.clone(), hp));
                    }
                }
            }
            case ATTACKING -> {
                if (target.isEmpty()) {
                    System.err.println("attempted to attack NOTHING!");
                    setFollowing();
                    break;
                }
                desiredPosition = Optional.of(target.get().getTransform().position);

                if (getTransform().position.distance(target.get().getTransform().position) < BITE_DISTANCE) {
                    if (biteStopwatch.hasElapsedAdvance(Duration.ofMillis(500))) {
                        // BITE
                        final var dmg = getEffect().computeDamage(
                            new DamageInfo(BASE_DAMAGE, target.get().getEntity(), null, getTransform().position.clone())
                                .setAttacker(entity)
                                .setColor(DamageColor.MELEE));
                        target.get().getHealth().damage(dmg);
                    }
                }
            }
        }

        if (state != State.HEALING) desiredPosition.ifPresent(p -> getTangible().velocity.moveTowardsEq(getTransform().position.directionTo(p).multiplyEq(MAX_SPEED), 1200 * infreqDelta()));
        else desiredPosition.ifPresent(p -> getTangible().velocity = getTransform().position.directionTo(p).multiplyEq(MAX_SPEED * 3));
    }

    private Vec2 getDesiredPlayerFollowPosition() {
        final var center = player.getTransform().position;
        
        double boost = state == State.HEALING ? 3 : 1;
        final double x = 100 * Math.cos(GameLoop.getUnpausedTime() * 3 * boost);
        final double y = 100 * Math.sin(GameLoop.getUnpausedTime() * 3 * boost);

        return center.add((float)x, (float)y);
    }

    public Optional<Unit> getTarget() {
        return target;
    }
}
