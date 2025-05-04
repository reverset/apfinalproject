package game.core;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.MoreMath;
import game.Shader;
import game.ShaderUpdater;
import game.Stopwatch;
import game.Tuple;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.comps.Transform;

public class Cube extends Unit {
    public static final int BASE_HEALTH = 1_000;
    public static final int BASE_DAMAGE = 20;
    public static final int BULLET_SPEED = 1_500;
    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(5);
    public static final Duration BURST_COOLDOWN = Duration.ofSeconds(2);
    public static final Duration BULLET_COOLDOWN_DURATION = Duration.ofMillis(200);
    
    public static final int TEXTURE_WIDTH = 400;
    public static final int TEXTURE_HEIGHT = 400;

    private Shader shader;
    private Raylib.RenderTexture renderTexture = Raylib.LoadRenderTexture(TEXTURE_WIDTH, TEXTURE_HEIGHT);

    private Stopwatch movementTimer = Stopwatch.ofGameTime();
    private Duration nextMoveTime = Duration.ofMillis(2_000);
    private Tween<Vec2> movementTween;
    private float playerDistance = 400;

    private float[] standardCubeColorCoeffs = new float[]{1.0f, 0.2f, 0.2f};
    private float[] shieldCubeColorCoeffs = new float[]{0.0f, 0.4f, 1.0f};

    private boolean isShieldUp = false;

    private Weapon2 weapon = null;

    private Stopwatch shieldEnableStopwatch = Stopwatch.ofGameTime();
    private Stopwatch shieldDisableStopwatch = Stopwatch.ofGameTime();

    private Rect rect;

    public static EntityOf<Unit> makeEntity(Vec2 position, int level) {
        EntityOf<Unit> entity = new EntityOf<>("THE CUBE", Unit.class);

        Effect effect = new Effect().setLevel(level);
        Supplier<Float> timeSupplier = () -> time();
        Shader shader = Shader.fromCacheOrLoad("resources/cube.frag");

        shader.setShaderValue("resolution", new Vec2(TEXTURE_WIDTH, TEXTURE_HEIGHT));
        
        entity
            .addComponent(shader)
            .addComponent(new Transform(position))
            .addComponent(new Tangible())
            .addComponent(new Rect(TEXTURE_WIDTH, TEXTURE_HEIGHT, Color.WHITE))
            .addComponent(effect)
            .addComponent(new Health(BASE_HEALTH, effect))
            .register(new HealthBar(new Vec2(), entity.name, true))
            .register(new ShaderUpdater(
                List.of(new Tuple<>("time", timeSupplier))))
            .register(new Physics(0, 0, new Vec2(-TEXTURE_WIDTH/2, -TEXTURE_HEIGHT/2)))
            .register(new PostMortem(GameLoop::safeDestroy)
                .addWill(e -> GameLoop.defer(RandomPowerup::showScreen))
            )
            .register(new Cube())
            .addTags(GameTags.ENEMY_TEAM);

        return entity;
    }

    private float[] getCubeColorCoeffs() {
        return !isShieldUp 
            ? standardCubeColorCoeffs
            : shieldCubeColorCoeffs;
    }

    @Override
    public void setup() {
        rect = require(Rect.class);

        getHealth().setMaxHealthAndHealth(BASE_HEALTH * Math.max(1, getEffect().getLevel()/4));

        shader = require(Shader.class);
        var postMortem = requireSystem(PostMortem.class);
        postMortem.addWill(i -> {
            Team.getTeamByTagOf(entity).grantExp(100);
            // player
            //     .ifPresent(p -> p.getExpAccumulator().accumulate(100));
        }).addWill(e -> GameLoop.defer(() -> Raylib.UnloadRenderTexture(renderTexture)));
        
        getEffect().addDamageRecievingResponse(info -> isShieldUp ? info.asHealing().damage() : info.damage());
        getEffect().addDamageScaling(info -> info.damage() * getEffect().getLevel()/10);

        // var playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);
        // player = playerEntity.flatMap(entity -> entity.getSystem(Player.class));
        // playerTransform = playerEntity.flatMap(entity -> entity.getComponent(Transform.class));

        weapon = new SimpleWeapon(
            BASE_DAMAGE, BULLET_SPEED, Color.RED, GameTags.ENEMY_TEAM_TAGS, BULLET_LIFETIME, BULLET_COOLDOWN_DURATION.toMillis()/1_000f, Optional.of(getEffect()));
        
        shieldEnableStopwatch.start();
    }

    @Override
    public void ready() {
        super.ready();
        movementTimer.start();
    }

    @Override
    public void infrequentUpdate() {
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();

        attackTick(target.getTransform());
        movementTick(target.getTransform());
        shieldTick();

    }

    @Override
    public void frame() { // Why is this rendered here, why not render()? When render() is called, the GameLoop is already drawing to a render texture. Raylib doesn't support drawing to multiple render textures at the same time.
        getTangible().velocity.setEq(0, 0);
        shader.setShaderValue("cubeColorCoeffs", getCubeColorCoeffs());
        shader.with(() -> {
            Raylib.BeginTextureMode(renderTexture);
            Raylib.ClearBackground(Jaylib.BLANK);
            Raylib.DrawTexture(renderTexture.texture(), 0, 0, Color.WHITE.getPointerNoUpdate());
            Raylib.EndTextureMode();
        });
    }

    private void altAttack() {
        GameLoop.safeTrack(Sigma.makeEntity(getTransform().position.clone(), Vec2.randomUnit().multiplyEq(1_000), getEffect().getLevel()));
    }

    private void attackTick(Transform plTrans) {
        if (!isShieldUp && weapon.canFire()) weapon.fire(getTransform().position.clone(), getTransform().position.directionTo(plTrans.position), entity);
    }

    private void shieldTick() {
        if (!isShieldUp) {
            if (shieldEnableStopwatch.hasElapsedAdvance(Duration.ofSeconds(5))) {
                isShieldUp = true;
                shieldDisableStopwatch.start();
                shieldEnableStopwatch.stop();

                for (int i = 0; i < 2; i++) {
                    altAttack();
                }
            }
        } else {
            if (shieldDisableStopwatch.hasElapsedAdvance(Duration.ofSeconds(2))) {
                isShieldUp = false;
                shieldDisableStopwatch.stop();
                shieldEnableStopwatch.start();
            }
        }
    }

    private void movementTick(Transform plTrans) {

        var currentAngle = (getTransform().position.minus(plTrans.position)).getAngle();
        if (movementTimer.hasElapsedAdvance(nextMoveTime)) {
            var desiredAngle = (float) MoreMath.random(0, MoreMath.TAU);
            
            if (movementTween != null) {
                movementTween.stop();
                movementTween = null;
            }
            
            movementTween = GameLoop.makeTweenGameTime(
                    Tween.circleLerp(currentAngle, desiredAngle, playerDistance, () -> plTrans.position), 1.2f, val -> {
                        getTransform().position = val;
            });
            movementTween
                .setDestroy(false)
                .start();
        } else {
            getTransform().position = plTrans.position.add(Vec2.fromAngle(currentAngle).multiplyEq(playerDistance));
        }

    }

    public boolean isShieldActive() {
        return isShieldUp;
    }

    @Override
    public void render() {
        Raylib.DrawTexture(renderTexture.texture(), getTransform().position.xInt() - rect.width/2, getTransform().position.yInt() - rect.height/2, Color.WHITE.getPointerNoUpdate());
        
        // Raylib.DrawCircle(trans.position.xInt(), trans.position.yInt(), 10, Color.WHITE.getPointerNoUpdate());
        // Raylib.DrawCircle(getCenter().xInt(), getCenter().yInt(), 10, Color.WHITE.getPointerNoUpdate());
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }
    
}
