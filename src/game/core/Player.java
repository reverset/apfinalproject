package game.core;

import com.raylib.Raylib;

import game.Color;
import game.Game;
import game.GameLoop;
import game.MoreMath;
import game.Text;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Player extends ECSystem implements Controllable {
    public static final float BULLET_SPEED = 500;
    public static final float MAX_SPEED = 200;

    public final Text healthText = new Text("N/A", new Vec2(Vec2.screen().x-100, 15), 54, Color.WHITE);

    public static Entity makeEntity() {
        return new Entity("Player")
            .addComponent(new Transform())
            .addComponent(new Health(50))
            .addComponent(new Rect(30, 30, Color.GREEN))
            .addComponent(new Tangible())
            .register(new RectRender())
            .register(new Physics())
            .register(new Player())
            .register(new Controller<>(Player.class))
            .addTag(GameTags.PLAYER);
    }

    private Tangible tangible;
    private Physics physics;
    private Transform trans;
    private Rect rect;
    private Health health;

    @Override
    public void setup() {
        tangible = require(Tangible.class);
        physics = requireSystem(Physics.class);
        trans = require(Transform.class);
        rect = require(Rect.class);
        health = require(Health.class);
    }

    @Override
    public void ready() {
        health.onDeath.listenOnce((v) -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(new Entity("Death Screen")
                .register(new ECSystem() {
                    private Text text = new Text("You died!", Vec2.screen().divideEq(2), 54, Color.WHITE).center();
                    private float originalX = text.position.x;

                    @Override
                    public void setup() {
                    }

                    @Override
                    public void frame() {
                        text.position.x = (float) (Math.sin(timeDouble()*5)*100 + originalX);

                        if (Raylib.IsKeyPressed(Raylib.KEY_ENTER)) {
                            GameLoop.clearAllEntities();
                            GameLoop.defer(() -> {
                                Game.loadLevel();
                            });
                        }
                    }

                    @Override
                    public void hudRender() {
                        text.render();
                    }
                    
                }));
        });
    }

    @Override
    public void frame() {
        Vec2 moveVector = controlledMoveVector();
        physics.applyForce(moveVector.multiply(500));
        // physics.applyForce(moveVector.multiply(800).divideEq(tangible.velocity.clone().absMinAllowedValue(1)));
        
        // Friction
        if (MoreMath.isApprox(moveVector.x, 0)) tangible.velocity.x = MoreMath.moveTowards(tangible.velocity.x, 0, 500 * delta());
        if (MoreMath.isApprox(moveVector.y, 0)) tangible.velocity.y = MoreMath.moveTowards(tangible.velocity.y, 0, 500 * delta());
        // tangible.velocity.clampEq(Vec2.one().multiply(100));

        GameLoop.getMainCamera().trans.position.lerpEq(trans.position, 2*delta());
    }

    private void fireBullet() {
        Vec2 direction = trans.position.add(new Vec2(rect.width*0.5f, rect.height*0.5f)).directionTo(GameLoop.getMousePosition());

        GameLoop.safeTrack(BulletFactory.standardBullet(new Transform(rect.getCenter(trans.position), trans.rotation), direction, BULLET_SPEED, Color.AQUA, entity));
    }

    @Override
    public void controlledClickOnce() {
        fireBullet();
    }

    @Override
    public void hudRender() {
        healthText.text = "" + health.getHealth();
        healthText.render();
    }

    // @Override
    // public void controlledLeftOnce() {
    //     tangible.velocity.x = 0;
    //     physics.impulse(Vec2.left().multiply(100));
    // }

    // @Override
    // public void controlledRightOnce() {
    //     tangible.velocity.x = 0;
    //     physics.impulse(Vec2.right().multiply(100));
    // }

    // @Override
    // public void controlledLeft() {
    //    physics.applyForce(Vec2.left().multiply(100));
    // }

    // @Override
    // public void controlledRight() {
    //     physics.applyForce(Vec2.right().multiply(100));
    // }

    // @Override
    // public void controlledDownOnce() {
    //     tangible.velocity.y = 0;
    //     physics.impulse(Vec2.down().multiply(100));
    // }

    // @Override
    // public void controlledDown() {
    //     physics.applyForce(Vec2.down().multiply(100));
    // }
    
    // @Override
    // public void controlledUpOnce() {
    //     tangible.velocity.y = 0;
    //     physics.impulse(Vec2.up().multiply(100));
    // }

    // @Override
    // public void controlledUp() {
    //     physics.applyForce(Vec2.up().multiply(100));
    // }
    
}
