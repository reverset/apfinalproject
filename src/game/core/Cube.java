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
    public static int TEXTURE_WIDTH = 400;
    public static int TEXTURE_HEIGHT = 400;

    private Shader shader;
    private Raylib.RenderTexture renderTexture = Raylib.LoadRenderTexture(TEXTURE_WIDTH, TEXTURE_HEIGHT);

    private Optional<Player> player;
    private Optional<Transform> playerTransform;
    private GameTimeStopwatch movementTimer = new GameTimeStopwatch();
    private Duration nextMoveTime = Duration.ofMillis(2_000);
    private Tween<Vec2> movementTween;
    private float playerDistance = 400;

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
            .register(new ShaderUpdater(List.of(new Tuple<>("time", timeSupplier))))
            .register(new Physics(0, 0))
            .register(new Cube())
            .addTags(GameTags.ENEMY_TEAM);

        return entity;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        health = require(Health.class);
        effect = require(Effect.class);

        shader = require(Shader.class);

        var playerEntity = GameLoop.findEntityByTag(GameTags.PLAYER);
        player = playerEntity.flatMap(entity -> entity.getSystem(Player.class));
        playerTransform = playerEntity.flatMap(entity -> entity.getComponent(Transform.class));
    }

    @Override
    public void ready() {
        movementTimer.start();
    }

    @Override
    public void infrequentUpdate() {
        movementTick();
    }

    @Override
    public void frame() { // Why is this rendered here, why not render()? When render() is called, the GameLoop is already drawing to a render texture. Raylib doesn't support drawing to multiple render textures at the same time.
        tangible.velocity.setEq(0, 0);
        shader.with(() -> {
            Raylib.BeginTextureMode(renderTexture);
            Raylib.ClearBackground(Jaylib.BLANK);
            Raylib.DrawTexture(renderTexture.texture(), 0, 0, Color.WHITE.getPointerNoUpdate());
            Raylib.EndTextureMode();
        });
    }

    private void movementTick() {
        if (player.isEmpty() || playerTransform.isEmpty()) return;
        movementTimer.tick(infreqDelta()*1_000);

        var pl = player.get();
        var plTrans = playerTransform.get();

        var currentAngle = (getCenter().minus(plTrans.position)).getAngle();
        if (movementTimer.hasElapsedAdvance(nextMoveTime)) {
            var desiredAngle = (float) MoreMath.random(0, Math.TAU);
            
            if (movementTween != null) {
                movementTween.stop();
                movementTween = null;
            }
            
            movementTween = GameLoop.makeTween(
                    Tween.circleLerp(currentAngle, MoreMath.randomSignumNonZero()*desiredAngle, playerDistance, () -> plTrans.position), 1.2f, val -> {
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
