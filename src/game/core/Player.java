package game.core;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.raylib.Raylib;

import game.Color;
import game.Game;
import game.GameLoop;
import game.MoreMath;
import game.Stopwatch;
import game.Text;
import game.Tween;
import game.TweenAnimation;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.ECSystem;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class Player extends ECSystem implements Controllable {
    public static final float MAX_SPEED = 200;
    public static final int SIZE = 30;

    public static final int BASE_DAMAGE = 5;
    public static final int BULLET_SPEED = 800;

    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(3);
    
    private final Text healthText = new Text("N/A", new Vec2(15, Vec2.screen().y-64), 54, new Color(255, 255, 255, 255));
    private final Text maxHealthText = new Text("N/A", new Vec2(120, Vec2.screen().y-64+(34/2)), 34, Color.ORANGE);

    private Tangible tangible;
    private Physics physics;
    private Transform trans;
    private Rect rect;
    private Health health;
    private Effect effect;

    private TweenAnimation healthPulseAnimation;
    private TweenAnimation healthCriticalVignette;

    private boolean warningNotifVisible = false;
    private Color warningColor = new Color(255, 0, 0, 0);
    private Rect warningRect = new Rect(800, 50, warningColor);
    private Text warningText = new Text("null", Vec2.screenCenter().addEq(0, -227), 54, Color.WHITE);
    private Stopwatch warningStopwatch = new Stopwatch();

    public static Entity makeEntity() {
        Effect effect = new Effect().setLevel(1);

        Entity entity = new Entity("Player");
        
        entity
            .addComponent(new Transform())
            .addComponent(new Health(200, effect))
            .addComponent(new Rect(SIZE, SIZE, Color.GREEN))
            .addComponent(new Tangible())
            .addComponent(effect)
            // .addComponent(new Shader("resources/pastelcycle.frag"))
            // .register(new ShaderUpdater(List.of(ShaderUpdater.timeUpdater())))
            .register(new RectRender())
            .register(new Physics(0, 0))
            .register(new Player())
            .register(new Controller<>(Player.class))
            // .register(new Diamond(entity, null, effect, 1))
            .addTags(GameTags.PLAYER, GameTags.PLAYER_TEAM);

        return entity;
    }

    private Weapon2 weapon;

    @Override
    public void setup() {
        tangible = require(Tangible.class);
        physics = requireSystem(Physics.class);
        trans = require(Transform.class);
        rect = require(Rect.class);
        health = require(Health.class);
        effect = require(Effect.class);

        final int INITIAL_FONT_SIZE = 54;
        final double HEALTH_PULSE_LENGTH = 0.1;
        final double HEALTH_DEFLATE_LENGTH = 2;

        healthPulseAnimation = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(INITIAL_FONT_SIZE, INITIAL_FONT_SIZE * 1.5), HEALTH_PULSE_LENGTH, v -> {
                healthText.fontSize = v.intValue();
                healthText.position.y = Vec2.screen().y - 10 - healthText.fontSize;
            }),
            new Tween<>(Tween.lerp(INITIAL_FONT_SIZE*1.5, INITIAL_FONT_SIZE), HEALTH_DEFLATE_LENGTH, v -> {
                healthText.fontSize = v.intValue();
                healthText.position.y = Vec2.screen().y - 10 - healthText.fontSize;
        })));

        healthCriticalVignette = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(0, 1), 0.1f, val -> {
                GameLoop.getPostProcessShader().ifPresent(shader -> {
                    shader.setShaderValue("vignetteStrength", val.floatValue());
                });
            }),
            new Tween<>(Tween.lerp(1, 0), 0.5f, val -> {
                GameLoop.getPostProcessShader().ifPresent(shader -> {
                    shader.setShaderValue("vignetteStrength", val.floatValue());
                });
            })
        ));

        entity.register(healthPulseAnimation);
        entity.register(healthCriticalVignette);

        weapon = new SimpleWeapon(BASE_DAMAGE, BULLET_SPEED, Color.AQUA, GameTags.PLAYER_TEAM_TAGS, BULLET_LIFETIME, 0.2f, Optional.of(effect));
        effect.onLevelUp.listen((level) -> {
            effect.addDamageScaling((info) -> info.absoluteDamageOrHeal() * level);
        }, entity);
    }

    public Vec2 getCenter() {
        return rect.getCenter(trans.position);
    }

    @Override
    public void ready() {
        health.onDamage.listen(v -> {
            healthPulseAnimation.start();

            if ((health.isCritical() || v.damage() >= health.getMaxHealth()*0.1f) && !healthCriticalVignette.isRunning()) {
                healthCriticalVignette.start();
            }
        }, entity);

        health.onDeath.listenOnce((v) -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(new Entity("Death Screen")
                .register(new ECSystem() {
                    private Text text = new Text("DEFEAT", Vec2.screenCenter().addEq(0, -100), 54, Color.WHITE).center();
                    private float originalX = text.position.x;
                    private Tween<Float> textTween;

                    @Override
                    public void setup() {
                        textTween = GameLoop.makeTween(Tween.lerp(400, 200), 0.2f, val -> {
                            text.fontSize = val.intValue();
                            text.position.x = originalX - text.measure()*0.35f;
                        }).start();
                    }

                    @Override
                    public void frame() {
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
        
        
        if (tangible.velocity.magnitude() > 300) {
            tangible.velocity.minusEq(tangible.velocity.normalize().multiplyEq(50));
        }
            
        physics.applyForce(moveVector.multiplyEq(2000));

        
        // Friction
        if (MoreMath.isApprox(moveVector.x, 0)) tangible.velocity.x = MoreMath.moveTowards(tangible.velocity.x, 0, 1000 * delta());
        if (MoreMath.isApprox(moveVector.y, 0)) tangible.velocity.y = MoreMath.moveTowards(tangible.velocity.y, 0, 1000 * delta());
        // tangible.velocity.clampEq(Vec2.one().multiply(100));

        GameLoop.getMainCamera().trans.position.lerpEq(trans.position, 2*delta());
    }

    private void tryFireWeapon() {
        if (weapon.canFire()) {
            Vec2 direction = trans.position.add(new Vec2(rect.width*0.5f, rect.height*0.5f)).directionTo(GameLoop.getMousePosition());
            weapon.fire(rect.getCenter(trans.position), direction, entity);
        }
    }

    @Override
    public void controlledClick() {
        tryFireWeapon();
    }

    @Override
    public void hudRender() {
        healthText.text = "" + health.getHealth();
        if (health.isCritical()) {

            var calc = (byte) ((255 / 2) * Math.sin(timeDouble()*10) + (255/2));
            
            healthText.color.g = calc;
            healthText.color.b = calc;
        } else {
            healthText.color.g = (byte) 255;
            healthText.color.b = (byte) 255;
        }
        maxHealthText.text = "/ " + health.getMaxHealth();
        
        maxHealthText.render();
        healthText.render();

        if (warningNotifVisible) { // probably want to abstract this away somewhere
            var desired = Vec2.screenCenter().addEq(0, -200);
            warningRect.color.a = (byte) ((Math.sin(timeDouble()*10)+1)*255*0.5);
            warningRect.render(warningRect.centerize(desired));
            warningText.position.x = desired.x - warningText.measure()*0.5f;
            warningText.render();

            if (warningStopwatch.hasElapsedSeconds(5)) {
                warningNotifVisible = false;
                warningStopwatch.stop();
            };
        }

        Raylib.DrawText("Objs: " + GameLoop.entityCount(), 15, 50, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Velocity: " + tangible.velocity, 15, 75, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Speed: " + tangible.velocity.magnitude(), 15, 102, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Bullets: " + BulletFactory.bullets.size(), 15, 124, 24, Color.WHITE.getPointer());
    }

    public void warningNotif(String message) {
        warningText.text = message;
        warningNotifVisible = true;
        warningStopwatch.restart();
    }

    public void animatedWarningNotif(String message, double durationSeconds) {
        warningText.text = "";
        GameLoop.makeTween(Tween.reveal(message), durationSeconds, val -> {
            warningText.text = val;
        }).start();
        
        warningNotifVisible = true;
        warningStopwatch.restart();
    }
}
