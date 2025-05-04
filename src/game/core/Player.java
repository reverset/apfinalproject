package game.core;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.raylib.Raylib;

import game.BetterButton;
import game.Color;
import game.DamageOverTime;
import game.Game;
import game.GameLoop;
import game.LoadingBar;
import game.MoreMath;
import game.Signal;
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

    public static final int HEALTH_BONUS_LEVEL_AMOUNT = 50;
    public static final int BASE_HEALTH = 100;

    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(3);
    public static final Duration iDuration = Duration.ofMillis(50);
    
    public Signal<Square> onKillEnemy = new Signal<>();    

    private final Text healthText = new Text("N/A", new Vec2(15, Vec2.screen().y-64), 54, new Color(255, 255, 255, 255));
    private final Text maxHealthText = new Text("N/A", new Vec2(120, Vec2.screen().y-64+(34/2)), 34, Color.ORANGE);
    private final Text levelText = new Text("0", new Vec2(Vec2.screen().x-100, Vec2.screen().y-50), 34, Color.ORANGE);

    private boolean allowLowHealthNotification = true;

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
    private Stopwatch warningStopwatch = Stopwatch.ofRealTime();
    private ExpAccumulator expAccumulator;

    public static Entity makeEntity() {
        Effect effect = new Effect().setLevel(1);

        Entity entity = new Entity("Player");
        effect.addDamageScaling(info -> effect.getLevel() * info.damage() + (int)(Math.pow(1.1, effect.getLevel())));
        
        entity
            .addComponent(new Transform())
            .addComponent(new Health(BASE_HEALTH, effect).withInvincibilityDuration(iDuration.toMillis() / 1_000f))
            .addComponent(new Rect(SIZE, SIZE, Color.GREEN))
            .addComponent(new Tangible())
            .addComponent(effect)
            // .addComponent(new Shader("resources/pastelcycle.frag"))
            // .register(new ShaderUpdater(List.of(ShaderUpdater.timeUpdater())))
            .register(new RectRender().centerize())
            .register(new Physics(0, 0, new Vec2(-SIZE/2, -SIZE/2)))
            .register(new ExpAccumulator(100))
            .register(new AutoTeamRegister())
            .register(new Player())
            .register(new Controller<>(Player.class))
            // .register(new Diamond(entity, null, effect, 1))
            // .register(new DamageOverTime(entity, null, effect, 3))
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
        expAccumulator = requireSystem(ExpAccumulator.class);

        final int INITIAL_FONT_SIZE = 54;
        final double HEALTH_PULSE_LENGTH = 0.1;
        final double HEALTH_DEFLATE_LENGTH = 2;

        health.setMaxHealthAndHealth(BASE_HEALTH + (HEALTH_BONUS_LEVEL_AMOUNT * (effect.getLevel() - 1)));

        healthPulseAnimation = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(INITIAL_FONT_SIZE, INITIAL_FONT_SIZE * 1.5), HEALTH_PULSE_LENGTH, v -> {
                healthText.fontSize = v.intValue();
                healthText.position.y = Vec2.screen().y - 10 - healthText.fontSize;
            }),
            new Tween<>(Tween.lerp(INITIAL_FONT_SIZE*1.5, INITIAL_FONT_SIZE), HEALTH_DEFLATE_LENGTH, v -> {
                healthText.fontSize = v.intValue();
                healthText.position.y = Vec2.screen().y - 10 - healthText.fontSize;
        })));

        int to = 2;
        int from = 0;
        healthCriticalVignette = new TweenAnimation(List.of(
            new Tween<>(Tween.lerp(from, to), 0.1f, val -> {
                GameLoop.getPostProcessShader().ifPresent(shader -> {
                    shader.setShaderValue("vignetteStrength", val.floatValue());
                });
            }),
            new Tween<>(Tween.lerp(to, from), 1.5f, val -> {
                GameLoop.getPostProcessShader().ifPresent(shader -> {
                    shader.setShaderValue("vignetteStrength", val.floatValue());
                });
            })
        ));

        entity.register(healthPulseAnimation);
        entity.register(healthCriticalVignette);

        weapon = new SimpleWeapon(BASE_DAMAGE, BULLET_SPEED, Color.AQUA, GameTags.PLAYER_TEAM_TAGS, BULLET_LIFETIME, 0.2f, Optional.of(effect));

        var xpBar = new Entity("xp bar")
            .addComponent(new Transform(new Vec2(0, Vec2.screen().y-10)))
            .addComponent(new Rect(Vec2.screen().xInt(), 10, Color.AQUA))
            .register(new LoadingBar(() -> expAccumulator.getXp(), expAccumulator.getMaxXp()));
        
        GameLoop.safeTrack(xpBar);

        effect.onLevelUp.listen(n -> {
            health.setMaxHealth(health.getMaxHealth() + HEALTH_BONUS_LEVEL_AMOUNT);

            // level up all powerups every 5 levels.
            if (n % 5 == 0) effect.getPowerups().forEach(Powerup::levelUp);
        }, entity);

        entity.setRenderPriority(100);
    }

    public ExpAccumulator getExpAccumulator() {
        return expAccumulator;
    }

    public Effect getEffect() {
        return effect;
    }

    public Health getHealth() {
        return health;
    }

    @Override
    public void ready() {
        GameLoop.safeTrack(HUD.makeEntity(this));

        health.onDamage.listen(v -> {
            healthPulseAnimation.start();

            if ((health.isCritical() || v.damage() >= health.getMaxHealth()*0.1f) && !healthCriticalVignette.isRunning()) {
                healthCriticalVignette.start();
                if (Settings.cameraShake) GameLoop.getMainCamera().shake(5);
            }

            if (health.isCritical() && !warningNotifVisible && allowLowHealthNotification) {
                animatedWarningNotif("LOW HEALTH", 1);
                allowLowHealthNotification = false;
            }

            if (!health.isCritical()) allowLowHealthNotification = true;

        }, entity);

        health.onDeath.listenOnce((v) -> {
            GameLoop.safeDestroy(entity);
            GameLoop.safeTrack(DestroyEffect.makeEntity(rect.dimensions(), trans.position.clone(), 2));

            GameLoop.makeTween(Tween.lerp(GameLoop.getMainCamera().trans.position, trans.position), 2, val -> {
                GameLoop.getMainCamera().trans.position = val;
            }).start();
            GameLoop.makeTween(Tween.lerp(GameLoop.getMainCamera().settings.zoom, 2), 2, val -> {
                GameLoop.getMainCamera().settings.zoom = val;
            }).start();

            GameLoop.makeTween(Tween.lerp(0.5f, 0), 2, val -> {
                GameLoop.timeScale = val;
            }).start().onFinish.listenOnce(n -> GameLoop.pause());

            GameLoop.runAfter(null, Duration.ofSeconds(2), () -> {
                // i should really just make a seperate class for this ...
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
                            textTween.entity.setPauseBehavior(true);
                            entity.setPauseBehavior(true);
                        }
    
                        @Override
                        public void ready() {
                            final BetterButton retryButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
                            retryButton
                                .setText("Retry")
                                .setTextColor(Color.WHITE)
                                .setOutlineThickness(4)
                                .setFontSize(34)
                                .centerize();
                            retryButton.onClick.listen((v) -> {
                                GameLoop.timeScale = 1;
                                GameLoop.clearAllEntities();
                                GameLoop.defer(() -> {
                                    Game.loadLevel();
                                });
                            });
    
                            final BetterButton mainMenuButton = new BetterButton(Color.WHITE, Color.BLUE, 8, 8);
                            mainMenuButton
                                .setText("Main Menu")
                                .setTextColor(Color.WHITE)
                                .setOutlineThickness(4)
                                .setFontSize(34)
                                .centerize();
                            mainMenuButton.onClick.listen((v) -> {
                                GameLoop.timeScale = 1;
                                GameLoop.defer(() -> {
                                    MainMenu.clearAndLoad();
                                });
                            });
    
                            GameLoop.safeTrack(new Entity("retryButton")
                                .addComponent(new Transform(Vec2.screen().addEq(-200, -50)))
                                .addComponent(new Rect(200, 50, Color.WHITE))
                                .setPauseBehavior(true)
                                .register(retryButton));
                            GameLoop.safeTrack(new Entity("mainMenuButtonDeathScreen")
                                .addComponent(new Transform(new Vec2(150, Vec2.screen().y-50)))
                                .addComponent(new Rect(200, 50, Color.WHITE))
                                .setPauseBehavior(true)
                                .register(mainMenuButton));
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
        });
    }


    @Override
    public void frame() {
        Vec2 moveVector = controlledMoveVector();
        if (Raylib.IsKeyPressed(Raylib.KEY_ESCAPE)) PauseMenu.open();
        
        physics.applyForce(moveVector.multiplyEq(2000));
        tangible.velocity.clampMagnitudeEq(400);

        
        // Friction
        if (MoreMath.isApprox(moveVector.x, 0)) tangible.velocity.x = MoreMath.moveTowards(tangible.velocity.x, 0, 2000 * delta());
        if (MoreMath.isApprox(moveVector.y, 0)) tangible.velocity.y = MoreMath.moveTowards(tangible.velocity.y, 0, 2000 * delta());
        // tangible.velocity.clampEq(Vec2.one().multiply(100));

        GameLoop.getMainCamera().trans.position.lerpEq(trans.position, 2*delta());
        if (Settings.dynamicZoom) {
            float zoom = GameLoop.getMainCamera().settings.zoom;
            GameLoop.getMainCamera().settings.zoom = MoreMath.lerp(zoom, 1 - (tangible.velocity.magnitude() / 400) * 0.1f, delta());
        }
    }

    private void tryFireWeapon() {
        if (weapon.canFire()) {
            Vec2 direction = trans.position.directionTo(GameLoop.getMousePosition());
            weapon.fire(trans.position.clone(), direction, entity);
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

        levelText.text = "Lv" + effect.getLevel();
        levelText.render();

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

        Raylib.DrawText("Entities: " + GameLoop.entityCount(), 15, 50, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Velocity: " + tangible.velocity, 15, 75, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Speed: " + tangible.velocity.magnitude(), 15, 102, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Bullets: " + BulletFactory.bullets.size(), 15, 124, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Resources: " + GameLoop.getResourceManager().countLoadedResources(), 15, 148, 24, Color.WHITE.getPointer());
        Raylib.DrawText("Position: " + trans.position, 15, 172, 24, Color.WHITE.getPointer());
    }

    public Transform getTransform() {
        return trans;
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
