package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.raylib.Raylib;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.MoreMath;
import game.Signal;
import game.Stopwatch;
import game.Vec2;
import game.ecs.comps.Transform;

public class TheRubinX extends Unit {
    public enum State {
        SPIN,
        X
    }

    public final Signal<State> onStateChange = new Signal<>();

    private static final int BASE_HEALTH = 5_000;
    private List<TheRubinXMinion> minions = new ArrayList<>();

    private State state = State.X;
    private Vec2 desiredPosition;

    private Stopwatch movemenStopwatch = Stopwatch.ofGameTime();

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
        e.setRenderPriority(50);

        return e;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        onStateChange.emit(state);
    }

    private void nextState() { // for debugging
        State[] vals = State.values();
        setState(vals[(state.ordinal() + 1) % vals.length]);
    }

    @Override
    public void setup() {
        GameLoop.defer(() -> {
            for (int i = 0; i < 9; i++) {
                final var minion = GameLoop.track(TheRubinXMinion.makeEntity(getTransform().position.clone(), getEffect().getLevel(), this, i));
                minions.add(minion.getMainSystem());
            }
        });
    }

    @Override
    public void frame() {
        if (Raylib.IsKeyPressed(Raylib.KEY_BACKSLASH)) {
            nextState();
        }

        if (desiredPosition == null) desiredPosition = getTransform().position.clone();
        getTransform().position.moveTowardsEq(desiredPosition, 500 * delta());
    }
    
    @Override
    public void infrequentUpdate() {
        if (movemenStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
            getTeam().findTarget(getTransform().position)
                .map(unit -> desiredPosition = unit.getTransform().position.addRandomByCoeff(500));
        }
    }

    @Override
    public void render() {
        // debug
        Raylib.DrawCircle(getTransform().position.xInt(), getTransform().position.yInt(), 10, Color.BLUE.getPointerNoUpdate());
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }
    
}
