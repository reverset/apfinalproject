package game;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import game.core.Effect;
import game.core.Enemy;
import game.core.GameTags;
import game.core.Physics;
import game.core.Player;
import game.core.SimpleWeapon;
import game.core.Tangible;
import game.core.Weapon2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Squiggy extends ECSystem {
    public enum State {
        FOLLOWING,
        ATTACKING
    }

    private final float MAX_SPEED = 500;
    private final float ATTACK_RADIUS = 300;

    private final int BASE_DAMAGE = 30;

    private Player player;
    private Transform playerTransform;
    private Entity playerEntity;
    private Vec2 desiredPosition;

    private Transform trans;
    private Tangible tangible;

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();
    private Stopwatch shootStopwatch = Stopwatch.ofGameTime();

    private State state = State.FOLLOWING;
    private Optional<Enemy> target = Optional.empty();

    private Weapon2 weapon = null;

    public static EntityOf<Squiggy> makeEntity(Entity player, int level) {
        EntityOf<Squiggy> e = new EntityOf<>("Squiggy", Squiggy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(30, 30, Color.WHITE))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .register(new RectRender().centerize()) // temp
            .register(new Physics(0, 0))
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

        Physics physics = requireSystem(Physics.class);
        physics.setImpulseResistance(400);

        Effect effect = require(Effect.class);

        weapon = new SimpleWeapon(BASE_DAMAGE, 400, Color.BLUE, GameTags.PLAYER_TEAM_TAGS, Duration.ofSeconds(1), 1, Optional.of(effect));
    }

    public void setLevel(int level) {
        System.out.println("level todo");
    }

    public State getState() {
        return state;
    }

    public Optional<Enemy> getTarget() {
        return target;
    }

    public void setFollowing() {
        state = State.FOLLOWING;
        target = Optional.empty();
    }

    public void setAttacking(Enemy target) {
        state = State.ATTACKING;
        this.target = Optional.of(target);
    }

    @Override
    public void infrequentUpdate() {
        float playerDistance = trans.position.distance(playerTransform.position);

        if (playerDistance > 1_000) {
            setFollowing();
        } else {
            if (target.isEmpty()) {
                List<Physics> potentialTargets = Physics.testCircle(trans.position, ATTACK_RADIUS, 0);
                for (final var pt : potentialTargets) {
                    Optional<Enemy> oEnemy = pt.entity.getSystem(Enemy.class);
                    if (oEnemy.isPresent()) {
                        setAttacking(oEnemy.get());
                        break;
                    }
                }
            } else {
                if (target.get().health.isDead()) {
                    setFollowing();
                } else {
                    desiredPosition = target.get().trans.position.clone();
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

        tangible.velocity.moveTowardsEq(trans.position.directionTo(desiredPosition).multiplyEq(MAX_SPEED), 1000 * delta());
    }
    
    private void followingProcess() {
        if (movementStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
            
            if (playerTransform.position.distance(trans.position) <= 100) {
                desiredPosition = trans.position.clone();
            } else {
                desiredPosition = playerTransform.position.clone();
            }
        }
    }

    private void attackingProcess() {

    }
    
}
