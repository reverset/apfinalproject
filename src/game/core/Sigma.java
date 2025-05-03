package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.comps.Transform;

public class Sigma extends Enemy {
    private static final int BASE_HEALTH = 10;
    private static final int BASE_DAMAGE = 50;
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final RayTexture TEXTURE = new RayImage("resources/sigma.png", WIDTH, HEIGHT).uploadToGPU();

    public static EntityOf<Enemy> makeEntity(Vec2 position, Vec2 velocity, int level) {
        EntityOf<Enemy> entity = new EntityOf<>("Sigma", Enemy.class);


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
            .register(new AutoTeamRegister())
            .register(new Sigma())
            .addTags(GameTags.ENEMY_TEAM_TAGS);


        return entity;
    }

    @Override
    public void setup() {
        basicSetup();
        health.setMaxHealthAndHealth(BASE_HEALTH + (BASE_HEALTH / 4 * (effect.getLevel() - 1)));
        effect.addDamageScaling(d -> d.damage() * effect.getLevel());
    }

    @Override
    public void ready() {
        // playerComp = player.flatMap(p -> p.getSystem(Player.class));
        health.onDeath.listen(e -> {
            GameLoop.safeDestroy(entity);
            Team.getTeamByTagOf(entity).grantExp(10);
        }, entity);

        tangible.onCollision.listen(other -> {
            if (!Team.getTeamByTagOf(entity).isOnMyTeam(other.entity)) {
                other.entity
                    .getComponent(Health.class)
                    .ifPresent(health -> {
                        health.damage(new DamageInfo(BASE_DAMAGE, other.entity, null, trans.position.clone())
                            .setAttacker(entity));
                });
                health.kill();
            }
        });
    }

    @Override
    public void infrequentUpdate() {
        // if (playerTransform == null || player.isEmpty()) return;
        Team team = Team.getTeamByTagOf(entity);
        final var ot = team.findTarget(trans.position);
        if (ot.isEmpty()) return;
        Target target = ot.get();

        tangible.velocity.moveTowardsEq(trans.position.directionTo(target.trans().position).multiplyEq(1000), 1000*infreqDelta());
    }

    @Override
    public void frame() {
        // if (playerTransform == null || player.isEmpty()) return;
        Team team = Team.getTeamByTagOf(entity);
        final var ot = team.findTarget(trans.position);
        if (ot.isEmpty()) return;
        Target target = ot.get();

        float angle = trans.position.directionTo(target.trans().position).getAngleDegrees();
        trans.rotation = angle;
    }
}
