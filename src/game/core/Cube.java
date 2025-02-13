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
import game.GameTimeStopwatch;
import game.MoreMath;
import game.Shader;
import game.ShaderUpdater;
import game.Tuple;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.ecs.comps.Transform;

public class Cube extends Enemy {
    public static final int BASE_HEALTH = 1_000;
    public static final int BASE_DAMAGE = 20;
    public static final int BULLET_SPEED = 2_000;
    public static final Duration BULLET_LIFETIME = Duration.ofSeconds(5);
    public static final Duration BURST_COOLDOWN = Duration.ofSeconds(2);
    public static final Duration BULLET_COOLDOWN_DURATION = Duration.ofMillis(200);
    
    public static final int TEXTURE_WIDTH = 400;
    public static final int TEXTURE_HEIGHT = 400;

    private Shader shader;
    private Raylib.RenderTexture renderTexture = Raylib.LoadRenderTexture(TEXTURE_WIDTH, TEXTURE_HEIGHT);

    private Optional<Player> player;
    private Optional<Transform> playerTransform;
    private GameTimeStopwatch movementTimer = new GameTimeStopwatch();
    private Duration nextMoveTime = Duration.ofMillis(2_000);
    private Tween<Vec2> movementTween;
    private float playerDistance = 400;

    private float[] standardCubeColorCoeffs = new float[]{1.0f, 0.4f, 0.4f};
    private float[] shieldCubeColorCoeffs = new float[]{0.0f, 1.0f, 1.0f};

    private boolean isShieldUp = false;

    private Weapon2 weapon = null;

    private GameTimeStopwatch shieldEnableStopwatch = new GameTimeStopwatch();
    private GameTimeStopwatch shieldDisableStopwatch = new GameTimeStopwatch();

    public static EntityOf<Enemy> makeEntity(Vec2 position, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("THE CUBE", Enemy.class);

        Effect effect = new Effect().setLevel(level);
        Supplier<Float> timeSupplier = () -> time();
        Shader shader = new Shader("resources/cube.frag");

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
            .register(new Physics(0, 0))
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
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);
        effect = require(Effect.class);

        shader = require(Shader.class);
        
        effect.addDamageRecievingResponse(info -> isShieldUp ? info.asHealing().damage() : info.damage());

        var playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);
        player = playerEntity.flatMap(entity -> entity.getSystem(Player.class));
        playerTransform = playerEntity.flatMap(entity -> entity.getComponent(Transform.class));

        weapon = new SimpleWeapon(
            BASE_DAMAGE, BULLET_SPEED, Color.RED, new Object[]{GameTags.ENEMY_TEAM}, BULLET_LIFETIME, BULLET_COOLDOWN_DURATION.toMillis()/1_000f, Optional.of(effect));
        
        shieldEnableStopwatch.bindTo(entity).start();
        shieldDisableStopwatch.bindTo(entity);
    }

    @Override
    public void ready() {
        movementTimer.start();
    }

    @Override
    public void infrequentUpdate() {
        playerTransform.ifPresent(plTrans -> {
            attackTick(plTrans);
            movementTick(plTrans);
            shieldTick();
        });

    }

    @Override
    public void frame() { // Why is this rendered here, why not render()? When render() is called, the GameLoop is already drawing to a render texture. Raylib doesn't support drawing to multiple render textures at the same time.
        tangible.velocity.setEq(0, 0);
        shader.setShaderValue("cubeColorCoeffs", getCubeColorCoeffs());
        shader.with(() -> {
            Raylib.BeginTextureMode(renderTexture);
            Raylib.ClearBackground(Jaylib.BLANK);
            Raylib.DrawTexture(renderTexture.texture(), 0, 0, Color.WHITE.getPointerNoUpdate());
            Raylib.EndTextureMode();
        });
    }

    private void attackTick(Transform plTrans) {
        if (weapon.canFire()) weapon.fire(getCenter(), getCenter().directionTo(plTrans.position), entity);
    }

    private void shieldTick() {
        if (!isShieldUp) {
            if (shieldEnableStopwatch.hasElapsedAdvance(Duration.ofSeconds(5))) {
                System.out.println("CUBE >> Shield up!");
                isShieldUp = true;
                shieldDisableStopwatch.start();
                shieldEnableStopwatch.stop();
            }
        } else {
            if (shieldDisableStopwatch.hasElapsedAdvance(Duration.ofSeconds(2))) {
                System.out.println("CUBE >> Shield Down!");
                isShieldUp = false;
                shieldDisableStopwatch.stop();
                shieldEnableStopwatch.start();
            }
        }
    }

    private void movementTick(Transform plTrans) {
        if (player.isEmpty() || playerTransform.isEmpty()) return;
        movementTimer.tick(infreqDelta()*1_000);

        var currentAngle = (getCenter().minus(plTrans.position)).getAngle();
        if (movementTimer.hasElapsedAdvance(nextMoveTime)) {
            var desiredAngle = (float) MoreMath.random(0, Math.TAU);
            
            if (movementTween != null) {
                movementTween.stop();
                movementTween = null;
            }
            
            movementTween = GameLoop.makeTween(
                    Tween.circleLerp(currentAngle, desiredAngle, playerDistance, () -> plTrans.position), 1.2f, val -> {
                        trans.position = fromCenter(val);
            });
            movementTween
                .setDestroy(false)
                .start();
        } else {
            trans.position = fromCenter(plTrans.position.add(Vec2.fromAngle(currentAngle).multiplyEq(playerDistance)));
        }

    }

    @Override
    public void render() {
        Raylib.DrawTexture(renderTexture.texture(), trans.position.xInt(), trans.position.yInt(), Color.WHITE.getPointerNoUpdate());
        
        // Raylib.DrawCircle(trans.position.xInt(), trans.position.yInt(), 10, Color.WHITE.getPointerNoUpdate());
        // Raylib.DrawCircle(getCenter().xInt(), getCenter().yInt(), 10, Color.WHITE.getPointerNoUpdate());
    }

    public Vec2 getCenter() {
        return trans.position.add(TEXTURE_WIDTH/2f, TEXTURE_HEIGHT/2f);
    }

    public Vec2 fromCenter(Vec2 offset) {
        return offset.minus(TEXTURE_WIDTH/2f, TEXTURE_HEIGHT/2f);
    }
    
}
