package game.core;

import java.util.ArrayList;
import java.util.List;

import com.raylib.Raylib;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.ecs.comps.Transform;

public class TheRubinX extends Unit {
    public enum State {
        SPIN
    }

    private static final int BASE_HEALTH = 5_000;
    private List<TheRubinXMinion> minions = new ArrayList<>();

    private State state = State.SPIN;

    public static EntityOf<Unit> makeEntity(Vec2 spawnPos, int level) {
        EntityOf<Unit> e = new EntityOf<Unit>("The Rubin X", Unit.class);

        e
            .addComponent(new Transform(spawnPos))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Health(BASE_HEALTH))
            .register(new HealthBar(Vec2.zero(), e.name, true))
            .register(new TheRubinX())
            .addTags(GameTags.ENEMY_TEAM_TAGS);

        return e;
    }

    public State getState() {
        return state;
    }

    @Override
    public void setup() {
        GameLoop.defer(() -> {
            for (int i = 0; i < 5; i++) {
                final var minion = GameLoop.track(TheRubinXMinion.makeEntity(getTransform().position.clone(), getEffect().getLevel(), this, i));
                minions.add(minion.getMainSystem());
            }
        });
    }

    @Override
    public void render() {
        // debug
        Raylib.DrawCircle(getTransform().position.xInt(), getTransform().position.yInt(), 10, Color.RED.getPointerNoUpdate());
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }
    
}
