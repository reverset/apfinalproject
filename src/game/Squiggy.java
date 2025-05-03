package game;

import java.time.Duration;
import java.util.Optional;

import game.core.ArcWeapon;
import game.core.AutoTeamRegister;
import game.core.DamageColor;
import game.core.Effect;
import game.core.Enemy;
import game.core.GameTags;
import game.core.Physics;
import game.core.Player;
import game.core.Tangible;
import game.core.Target;
import game.core.Team;
import game.core.Weapon2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

// todo: if player dies, squiggy will keep the game going in the defeat screen.
public class Squiggy extends ECSystem {
    public enum State {
        FOLLOWING,
        ATTACKING
    }

    private static final int SIZE = 100;
    private static final RayTexture TEXTURE = new RayImage("resources/squiggy.png", SIZE, SIZE).uploadToGPU();

    private final float MAX_SPEED = 500;
    private final float ATTACK_RADIUS = 400;

    private final int BASE_DAMAGE = 10;

    private Player player;
    private Transform playerTransform;
    private Entity playerEntity;
    private Vec2 desiredPosition;

    private Transform trans;
    private Tangible tangible;

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();

    private State state = State.FOLLOWING;
    private Optional<Target> target = Optional.empty();

    private Weapon2 weapon = null;
    private Effect effect = null;

    private TextureRenderer textureRenderer;

    public static EntityOf<Squiggy> makeEntity(Entity player, int level) {
        EntityOf<Squiggy> e = new EntityOf<>("Squiggy", Squiggy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(SIZE, SIZE, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            // .register(new RectRender().centerize()) // temp
            .register(new TextureRenderer(TEXTURE))
            .register(new Physics(0, 0))
            .register(new AutoTeamRegister())
            .register(new Squiggy(player))
            .addTags(GameTags.PLAYER_TEAM_TAGS);
    
        return e;
    }

    public Squiggy(Entity player) {
        playerEntity = player;
        this.player = player.getSystem(Player.class).orElseThrow(() -> new RecoverableException("no player system"));
        this.playerTransform = player.getComponent(Transform.class).orElseThrow(() -> new RecoverableException("no player transform"));
    }

    @Override
    public void ready() {
        trans.position = playerTransform.position.clone();
        desiredPosition = trans.position.clone();

        movementStopwatch.start();
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        effect = require(Effect.class);
        effect.addDamageScaling(info -> effect.getLevel() * info.damage());
        
        Rect rect = require(Rect.class);

        Physics physics = requireSystem(Physics.class);
        physics.setImpulseResistance(400);


        textureRenderer = requireSystem(TextureRenderer.class);
        physics.setHitboxOffset(new Vec2(-textureRenderer.getTexture().width()/2, -textureRenderer.getTexture().height()/2));
        // rect.width = textureRenderer.getTexture().width();
        // rect.height = textureRenderer.getTexture().height();

        // weapon = new SimpleWeapon(BASE_DAMAGE, 1_000, Color.BLUE, GameTags.PLAYER_TEAM_TAGS, Duration.ofSeconds(1), 0.5f, Optional.of(effect));
        weapon = new ArcWeapon(BASE_DAMAGE, (float)(Math.PI/4), 5, 1_000, Color.BLUE, GameTags.PLAYER_TEAM_TAGS, 0.5f, Duration.ofSeconds(1), Optional.of(effect));
        weapon.setHitMarkerColor(DamageColor.SPECIAL);

        entity.setRenderPriority(90);
    }

    public void setLevel(int level) {
        effect.setLevel(level);
    }

    public State getState() {
        return state;
    }

    public Optional<Target> getTarget() {
        return target;
    }

    public void setFollowing() {
        state = State.FOLLOWING;
        target = Optional.empty();
    }

    public void setAttacking(Enemy target) {
        state = State.ATTACKING;
        this.target = Optional.of(Target.ofEntity(target.entity));
    }

    @Override
    public void frame() {
        trans.rotation = (tangible.velocity.x / MAX_SPEED) * 8;
    }

    @Override
    public void infrequentUpdate() {
        float playerDistance = trans.position.distance(playerTransform.position);
        Team team = Team.getTeamByTagOf(entity);

        if (playerDistance > 1_000) {
            setFollowing();
        } else {
            if (target.isEmpty()) {
                target = team.findTarget(trans.position);
                
                if (target.isPresent()) state = State.ATTACKING;
            } else {
                if (target.get().health().get().isDead() || !team.shouldEntityBeTargetted(target.get().entity())) {
                    setFollowing();
                } else {
                    desiredPosition = target.get().trans().position.clone();
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

        tangible.velocity.moveTowardsEq(trans.position.directionTo(desiredPosition).multiplyEq(MAX_SPEED), 1200 * infreqDelta());
    }
    
    private void followingProcess() {
        if (movementStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
            
            if (playerTransform.position.distance(trans.position) <= 300) {
                desiredPosition = trans.position.clone();
            } else {
                desiredPosition = playerTransform.position.clone();
            }
        }
    }

    private void attackingProcess() {
        weapon.fire(trans.position.clone(), trans.position.directionTo(target.get().trans().position), entity);
    }
    
}
