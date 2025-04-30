package game;

import game.core.Physics;
import game.core.Player;
import game.core.Tangible;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Squiggy extends ECSystem {
    enum State {
        FOLLOWING,
        ATTACKING
    }

    private final float MAX_SPEED = 200;

    private Player player;
    private Transform playerTransform;
    private Entity playerEntity;
    private Vec2 desiredPosition;

    private Transform trans;
    private Tangible tangible;

    private State state = State.FOLLOWING;

    public static EntityOf<Squiggy> makeEntity(Entity player) {
        EntityOf<Squiggy> e = new EntityOf<>("Squiggy", Squiggy.class);

        e
            .addComponent(new Transform())
            .addComponent(new Rect(30, 30, Color.WHITE))
            .addComponent(new Tangible())
            .register(new RectRender().centerize()) // temp
            .register(new Physics(0, 0))
            .register(new Squiggy(player));
    
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
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
    }

    public void setLevel(int level) {
        System.out.println("level todo");
    }

    @Override
    public void infrequentUpdate() {
        switch (state) {
            case FOLLOWING:
                followingProcess();
                break;
        
            case ATTACKING:
                attackingProcess();
                break;
        }
    }
    
    private void followingProcess() {
        tangible.velocity.moveTowardsEq(trans.position.directionTo(desiredPosition).multiplyEq(MAX_SPEED), delta());
    }

    private void attackingProcess() {

    }
    
}
