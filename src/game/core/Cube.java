package game.core;

import java.util.List;
import java.util.function.Supplier;

import game.Color;
import game.EntityOf;
import game.Shader;
import game.ShaderUpdater;
import game.Tuple;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.RectRender;
import game.ecs.comps.Transform;

import com.raylib.Raylib;
import com.raylib.Jaylib;

public class Cube extends Enemy {
    public static final int BASE_HEALTH = 1_000;
    public static int TEXTURE_WIDTH = 400;
    public static int TEXTURE_HEIGHT = 400;

    private Shader shader;
    private Raylib.RenderTexture renderTexture = Raylib.LoadRenderTexture(TEXTURE_WIDTH, TEXTURE_HEIGHT);

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
    }

    @Override
    public void frame() {
        shader.with(() -> {
            Raylib.BeginTextureMode(renderTexture);
            Raylib.ClearBackground(Jaylib.BLANK);
            Raylib.DrawTexture(renderTexture.texture(), 0, 0, Color.WHITE.getPointerNoUpdate());
            Raylib.EndTextureMode();
        });
    }

    @Override
    public void render() {
        Raylib.DrawTexture(renderTexture.texture(), trans.position.xInt(), trans.position.yInt(), Color.WHITE.getPointerNoUpdate());
    }
    
}
