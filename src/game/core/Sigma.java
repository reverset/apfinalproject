package game.core;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.comps.Transform;

public class Sigma extends Unit {
    private static final int BASE_HEALTH = 25;
    private static final int BASE_DAMAGE = 50;
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private long spawnTime = 0;

    private static final RayTexture TEXTURE = new RayImage("resources/sigma.png", WIDTH, HEIGHT).uploadToGPU();

    public static EntityOf<Unit> makeEntity(Vec2 position, Vec2 velocity, int level) {
        EntityOf<Unit> entity = new EntityOf<>("Sigma", Unit.class);


        entity
            .addComponent(new Rect(WIDTH, HEIGHT, Color.RED))
            .addComponent(new Transform(position))
            .addComponent(new Health(BASE_HEALTH))
            .addComponent(new Tangible(velocity))
            .addComponent(new Effect().setLevel(level))
            .register(new Physics(0, 0, new Vec2(-WIDTH/2, -HEIGHT/2)))
            // .register(new RectRender().centerize())
            .register(new TextureRenderer(TEXTURE))
            .register(new HealthBar(new Vec2(-WIDTH, -HEIGHT), entity.name))
            .register(new Sigma())
            .addTags(GameTags.ENEMY_TEAM_TAGS);


        return entity;
    }

    @Override
    public void setup() {
        getHealth().setMaxHealthAndHealth(BASE_HEALTH + (BASE_HEALTH / 4 * (getEffect().getLevel() - 1)));
        getEffect().addDamageScaling(d -> d.damage() * getEffect().getLevel());
    }

    @Override
    public void ready() {
        super.ready();
        spawnTime = System.currentTimeMillis();

        getHealth().onDeath.listen(e -> {
            GameLoop.safeDestroy(entity);
            Team.getTeamByTagOf(entity).grantExp(10);
        }, entity);

        getTangible().onCollision.listen(other -> {
            if (!Team.getTeamByTagOf(entity).isOnMyTeam(other.entity)) {
                other.entity
                    .getComponent(Health.class)
                    .ifPresent(health -> {
                        health.damage(new DamageInfo(BASE_DAMAGE, other.entity, null, getTransform().position.clone())
                            .setAttacker(entity));
                });
                getHealth().kill();
            }
        });
    }

    @Override
    public void infrequentUpdate() {
        // if (playerTransform == null || player.isEmpty()) return;
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();

        float elapsed = (float) ((System.currentTimeMillis() - (double)spawnTime) / 4_000);
        getTangible().velocity.moveTowardsEq(getTransform().position.directionTo(target.getTransform().position).multiplyEq(Math.min(2_000, 300 * elapsed)), 1000*infreqDelta());
        // getTangible().velocity = getTransform().position.directionTo(target.getTransform().position).multiplyEq(200);
    }

    @Override
    public void frame() {
        // if (playerTransform == null || player.isEmpty()) return;
        final var ot = getTeam().findTarget(getTransform().position);
        if (ot.isEmpty()) return;
        Unit target = ot.get();

        float angle = getTransform().position.directionTo(target.getTransform().position).getAngleDegrees();
        getTransform().rotation = angle;
    }
}
