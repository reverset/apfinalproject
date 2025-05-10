package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.comps.Transform;

public class TheRubinXMinion extends Unit {

    private TheRubinX master;
    private int index;

    private static final int BASE_DAMAGE = 40;

    private Weapon2 weapon;
    private Optional<Vec2> desiredPositionDuringSpin = Optional.empty();
    private Stopwatch dashStopwatch = Stopwatch.ofGameTime();
    private boolean canDash = false;

    public static EntityOf<TheRubinXMinion> makeEntity(Vec2 pos, int level, TheRubinX master, int index) {
        EntityOf<TheRubinXMinion> e = new EntityOf<>("The Rubin X Minion", TheRubinXMinion.class);

        final int X_WIDTH = 18;
        final int X_LENGTH = 70;

        e
            .addComponent(new Health(master.getHealth().getMaxHealth())) // health is managed by TheRubinX.java
            .addComponent(new Transform(pos))
            .addComponent(new Tangible())
            .addComponent(new Rect(X_LENGTH, X_LENGTH, Color.WHITE))
            .addComponent(new X(pos, Color.RED, X_WIDTH, X_LENGTH))
            .addComponent(new Effect().setLevel(level)) // perhaps use the effect present in TheRubinX?
            .register(new Physics(0, 0, new Vec2(-X_LENGTH/2, -X_LENGTH/2)))
            .register(new XRenderer())
            .register(new TheRubinXMinion(master, index))
            .addTags(GameTags.ENEMY_TEAM_TAGS);

        return e;
    }

    public TheRubinXMinion(TheRubinX master, int index) {
        this.master = master;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void infrequentUpdate() {
        getTangible().velocity.moveTowardsEq(Vec2.ZERO, 1_000 * infreqDelta());

        Optional<Unit> target = getTeam().findTarget(getTransform().position);

        if (master.getState() == TheRubinX.State.X) {
            Optional<Vec2> dir = target
                .map(unit -> {
                    Vec2 fp = unit.getFuturePosition(0.5f);
                    return getTransform().position.directionTo(fp);
                });
            if (dir.isEmpty()) return;
            if (weapon.canFire()) weapon.fire(getTransform().position.clone(), dir.get(), entity);
        }

        else if (master.getState() == TheRubinX.State.SPIN && !canDash) {
            if (dashStopwatch.hasElapsedAdvance(Duration.ofSeconds(index+1))) {
                if (target.isPresent()) {
                    Vec2 direction = getTransform().position.directionTo(target.get().getTransform().position);
                    desiredPositionDuringSpin = Optional.of(getTransform().position.add(direction.multiplyEq(1_000)));
                }
                canDash = true;
            }
        }
    }
    
    private void regularMovement() {
        Vec2 center = master.getTransform().position.clone();
        Vec2 desiredPosition = new Vec2();
        switch (master.getState()) {
            case SPIN -> {
                final float spinMagnitude = (float) ((Math.cos(GameLoop.getUnpausedTime()) * 100) + 200);
                if (master.getState() == TheRubinX.State.SPIN) {
                    center.addEq(
                        (float)Math.cos(index*0.7 + GameLoop.getUnpausedTime())*spinMagnitude, 
                        (float)Math.sin(index*0.7 + GameLoop.getUnpausedTime())*spinMagnitude);
                    desiredPosition.setEq(center.x, center.y);
                }
            }
            case X -> {
                if (index < 5) {
                    desiredPosition.setEq(center.x - index*50 + (100), center.y - index*50 + (100));
                } else if (index < 7) {
                    desiredPosition.setEq(center.x + (index-4)*50, center.y - (index-4)*50);
                } else {
                    desiredPosition.setEq(center.x - (index-6)*50, center.y + (index-6)*50);
                }
            }
        };
    
        getTransform().position.lerpEq(desiredPosition, 10 * delta());
    }

    private void dashMovement() {
        if (!canDash) return;
        getTransform().position.moveTowardsEq(desiredPositionDuringSpin.get(), 1_000 * delta());
    }

    @Override
    public void frame() {
        if (master.getState() == TheRubinX.State.SPIN && desiredPositionDuringSpin.isPresent()) {
            dashMovement();
        } else {
            regularMovement();
        }
    }

    @Override
    public void setup() {
        getHealth().onDamage.listen(master.getHealth()::damage, entity);

        master.onStateChange.listen(newState -> {
            weapon.restartCooldown();
            canDash = false;
            if (newState == TheRubinX.State.SPIN) {
                dashStopwatch.restart();
            }
        }, entity);
        weapon = new SimpleWeapon(BASE_DAMAGE, 1000, Color.RED, GameTags.ENEMY_TEAM_TAGS, Duration.ofSeconds(5), index+1, Optional.of(getEffect())).setTailLength(100);
    }
    
}
