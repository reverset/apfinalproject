package game.core;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.X;
import game.core.rendering.XRenderer;
import game.ecs.comps.Transform;

public class TheRubinXMinion extends Unit {

    private TheRubinX master;
    private int index;

    public static EntityOf<TheRubinXMinion> makeEntity(Vec2 pos, int level, TheRubinX master, int index) {
        EntityOf<TheRubinXMinion> e = new EntityOf<>("The Rubin X Minion", TheRubinXMinion.class);

        final int X_WIDTH = 18;
        final int X_LENGTH = 70;

        e
            .addComponent(new Health(Integer.MAX_VALUE)) // health is managed by TheRubinX.java
            .addComponent(new Transform(pos))
            .addComponent(new Tangible())
            .addComponent(new Rect(X_LENGTH, X_LENGTH, Color.WHITE))
            .addComponent(new X(pos, Color.RED, X_WIDTH, X_LENGTH))
            .addComponent(new Effect().setLevel(level))
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
    }

    @Override
    public void frame() {
        final float spinMagnitude = 500;
        if (master.getState() == TheRubinX.State.SPIN) {
            Vec2 center = master.getTransform().position.clone();
            center.addEq(
                (float)Math.cos(index + GameLoop.getUnpausedTime())*spinMagnitude, 
                (float)Math.sin(index + GameLoop.getUnpausedTime())*spinMagnitude);
            getTransform().position.setEq(center.x, center.y);
        }
    }

    @Override
    public void setup() {
        getHealth().onDamage.listen(master.getHealth()::damage, entity);
    }
    
}
