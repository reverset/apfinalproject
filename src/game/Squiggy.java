package game;

import java.time.Duration;
import java.util.Optional;

import game.core.ArcWeapon;
import game.core.DamageColor;
import game.core.Effect;
import game.core.GameTags;
import game.core.Health;
import game.core.Physics;
import game.core.Tangible;
import game.core.Unit;
import game.core.Weapon2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Squiggy extends Unit {
    public enum State {
        FOLLOWING,
        ATTACKING
    }

    private static final int SIZE = 100;
    private static final RayTexture TEXTURE = new RayImage("resources/squiggy.png", SIZE, SIZE).uploadToGPU();

    private final float MAX_SPEED = 500;

    private final int BASE_DAMAGE = 10;

    private Transform playerTransform;
    private Vec2 desiredPosition;

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();

    private State state = State.FOLLOWING;
    private Optional<Unit> target = Optional.empty();

    private Weapon2 weapon = null;

    private TextureRenderer textureRenderer;

    public static EntityOf<Squiggy> makeEntity(Entity player, int level) {
        EntityOf<Squiggy> e = new EntityOf<>("Squiggy", Squiggy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(SIZE, SIZE, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .addComponent(Health.ofInvincible())
            .register(new TextureRenderer(TEXTURE))
            .register(new Physics(0, 0))
            .register(new Squiggy(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);
    
        return e;
    }

    public Squiggy(Entity player) {
        this.playerTransform = player.getComponent(Transform.class).orElseThrow(() -> new RecoverableException("no player transform"));
    }

    @Override
    public void ready() {
        getTransform().position = playerTransform.position.clone();
        desiredPosition = getTransform().position.clone();

        movementStopwatch.start();
    }

    @Override
    public void setup() {
        getEffect().addDamageScaling(info -> getEffect().getLevel() * info.damage());
        
        Rect rect = require(Rect.class);

        Physics physics = requireSystem(Physics.class);
        physics.setImpulseResistance(400);


        textureRenderer = requireSystem(TextureRenderer.class);
        physics.setHitboxOffset(new Vec2(-textureRenderer.getTexture().width()/2, -textureRenderer.getTexture().height()/2));
        // rect.width = textureRenderer.getTexture().width();
        // rect.height = textureRenderer.getTexture().height();

        // weapon = new SimpleWeapon(BASE_DAMAGE, 1_000, Color.BLUE, GameTags.PLAYER_TEAM_TAGS, Duration.ofSeconds(1), 0.5f, Optional.of(effect));
        weapon = new ArcWeapon(BASE_DAMAGE, (float)(Math.PI/4), 5, 1_000, Color.BLUE, GameTags.PLAYER_TEAM_TAGS, 0.5f, Duration.ofSeconds(1), Optional.of(getEffect()));
        weapon.setHitMarkerColor(DamageColor.SPECIAL);

        entity.setRenderPriority(90);
    }

    public void setLevel(int level) {
        getEffect().setLevel(level);
    }

    public State getState() {
        return state;
    }

    public Optional<Unit> getTarget() {
        return target;
    }

    public void setFollowing() {
        state = State.FOLLOWING;
        target = Optional.empty();
    }

    public void setAttacking(Unit target) {
        state = State.ATTACKING;
        this.target = Optional.of(target);
    }

    @Override
    public void frame() {
        getTransform().rotation = (getTangible().velocity.x / MAX_SPEED) * 12;
    }

    @Override
    public void infrequentUpdate() {
        float playerDistance = getTransform().position.distance(playerTransform.position);

        if (playerDistance > 1_000) {
            setFollowing();
        } else {
            if (target.isEmpty()) {
                target = getTeam().findTarget(getTransform().position);
                
                if (target.isPresent()) state = State.ATTACKING;
            } else {
                if (target.get().getHealth().isDead() || !getTeam().shouldEntityBeTargetted(target.get().getEntity())) {
                    setFollowing();
                } else {
                    desiredPosition = target.get().getTransform().position.clone();
                }
            }
        }

        switch (state) {
            case FOLLOWING:
                followingProcess();
                break;
        
            case ATTACKING:
                attackingProcess();
                break;
        }

        getTangible().velocity.moveTowardsEq(getTransform().position.directionTo(desiredPosition).multiplyEq(MAX_SPEED), 1200 * infreqDelta());
    }
    
    private void followingProcess() {
        if (movementStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
            
            if (playerTransform.position.distance(getTransform().position) <= 300) {
                desiredPosition = getTransform().position.clone();
            } else {
                desiredPosition = playerTransform.position.clone();
            }
        }
    }

    private void attackingProcess() {
        weapon.fire(getTransform().position.clone(), getTransform().position.directionTo(target.get().getTransform().position), entity);
    }
    
}
