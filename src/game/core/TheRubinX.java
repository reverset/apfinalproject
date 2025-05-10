package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Signal;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.comps.Transform;

public class TheRubinX extends Unit {
    public enum State {
        SPIN,
        X
    }

    public final Signal<State> onStateChange = new Signal<>();

    private static final int BASE_HEALTH = 5_000;
    private static final int BASE_DAMAGE = 2_000;
    private List<TheRubinXMinion> minions = new ArrayList<>();

    private State state = State.X;
    private Vec2 desiredPosition;

    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();
    private Stopwatch stateChangeStopwatch = Stopwatch.ofGameTime();

    private ArrayList<Vec2> weaponDirections = new ArrayList<>();

    private ArrayList<LaserWeapon> weapons = new ArrayList<>();
    private Stopwatch shootStopwatch = Stopwatch.ofGameTime();

    public static EntityOf<Unit> makeEntity(Vec2 spawnPos, int level) {
        EntityOf<Unit> e = new EntityOf<Unit>("The Rubin X", Unit.class);

        e
            .addComponent(new Transform(spawnPos))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Health(BASE_HEALTH))
            .addComponent(new Rect(100, 100, Color.WHITE))
            .addComponent(new X(spawnPos, Color.PINK, 50, 100))
            .register(new HealthBar(Vec2.zero(), e.name, true))
            .register(new XRenderer())
            .register(new Physics(0, 0, new Vec2(-50, -50)))
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

    private void nextState() {
        State[] vals = State.values();
        setState(vals[(state.ordinal() + 1) % vals.length]);
    }

    @Override
    public void setup() {
        getHealth().onDeath.listenOnce(n -> {
            GameLoop.safeDestroy(entity);
            minions.stream()
                .map(Unit::getHealth)
                .filter(Health::isAlive)
                .forEach(Health::kill);
        });

        for (int i = 0; i < 30; i++) {
            weapons.add(new LaserWeapon(BASE_DAMAGE, getTransform().position, new Vec2(), 1, Color.PINK, 4_000, 200, 30, 0, GameTags.ENEMY_TEAM_TAGS, 1, Optional.of(getEffect())));
            weaponDirections.add(null);
        }

        entity.hide();
        GameLoop.defer(() -> {
            for (int i = 0; i < 9; i++) {
                final var minion = GameLoop.track(TheRubinXMinion.makeEntity(getTransform().position.clone(), getEffect().getLevel(), this, i));
                minions.add(minion.getMainSystem());
            }
        });
    }

    @Override
    public void ready() {
        getTangible().setTangible(false);
        stateChangeStopwatch.restart();
    }

    private void lastPhaseFrame() {
        getTangible().velocity.setEq(0, 0);
        Optional<Unit> target = getTeam().findTarget(getTransform().position);
        target.ifPresent(unit -> {
            if (shootStopwatch.hasElapsedAdvance(Duration.ofSeconds(2))) {
                for (int i = 0; i < weapons.size(); i++) {
                    LaserWeapon weapon = weapons.get(i);
                    final int j = i;
                    GameLoop.runAfter(entity, Duration.ofMillis(100 * (i+1)), () -> {
                        Vec2 desiredPos = null;
                        if (weaponDirections.get(j) == null) desiredPos = getTransform().position.directionTo(unit.getTransform().position);
                        else desiredPos = weaponDirections.get(j);
    
                        final Vec2 dp = desiredPos;
                        weapon.chargeUp(() -> getTransform().position, () -> dp, entity, impending -> {
                            // weaponDirections.set(j, impending);
                            if (impending && weaponDirections.get(j) == null) weaponDirections.set(j, getTransform().position.directionTo(unit.getTransform().position));
                            else weaponDirections.set(j, null);
                        });
                    });
                }
            }
        });
    }

    @Override
    public void frame() {
        if (entity.isVisible()) lastPhaseFrame();
        else {
            if (desiredPosition == null) desiredPosition = getTransform().position.clone();
            getTransform().position.moveTowardsEq(desiredPosition, 500 * delta());
        }
    }
    
    @Override
    public void infrequentUpdate() {
        if (minions.stream()
            .map(Unit::getHealth)
            .anyMatch(Health::isAlive)) {
            if (movementStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
                getTeam().findTarget(getTransform().position)
                    .map(unit -> desiredPosition = unit.getTransform().position.addRandomByCoeff(500));
            }
            if (stateChangeStopwatch.hasElapsedAdvance(Duration.ofSeconds(5))) {
                nextState();
            }
        } else {
            // final phase
            if (entity.isHidden()) {
                entity.show();
                getTangible().setTangible(true);
            }
        }
    }

    @Override
    public void render() {
        weapons.forEach(LaserWeapon::render);
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }
    
}
