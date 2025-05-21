package game.core;

import game.Color;
import game.ConditionalDespawn;
import game.DespawnDistance;
import game.EntityOf;
import game.GameLoop;
import game.MoreMath;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.comps.Transform;

public class Spirit extends Unit {

    private static final RayTexture texture = new RayImage("resources/spirit.png", 100, 100).uploadToGPU();

    private static final float SPAWN_DISTANCE = 500;
    private static final float SPEED = 100;

    public static EntityOf<Spirit> makeEntity(Vec2 pos, int level) {
        EntityOf<Spirit> e = new EntityOf<>("Spirit", Spirit.class);

        e
            .addComponent(new Transform(pos))
            .addComponent(new Tangible())
            .addComponent(new Rect(texture.width(), texture.height(), Color.WHITE))
            .addComponent(Health.ofInvincible())
            .addComponent(new Effect().setLevel(level))
            .register(new TextureRenderer(texture))
            .register(new Physics(2, 0, new Vec2(-texture.width()/2, -texture.height()/2)))
            .register(new Spirit())
            .addTags(GameTags.PLAYER_TEAM_TAGS);

        return e;
    }

    public static EntityOf<Spirit> spawn(int level) {
        Border border = Border.getInstance();
        Vec2 spawnPos = border.getCenter().addRandomByCoeff(border.getRadius() + SPAWN_DISTANCE);

        EntityOf<Spirit> spirit = makeEntity(spawnPos, level);

        GameLoop.safeTrack(spirit);

        return spirit;
    }

    @Override
    public void setup() {
        Vec2 directionToCenter = getTransform().position.directionTo(Border.getInstance().getCenter());
        Vec2 desiredOffset = directionToCenter.addByAngleOffsetDegrees((float)MoreMath.random(-15, 15));
        
        getTangible().velocity = desiredOffset.multiplyEq(SPEED);

        entity.registerDeferred(new ConditionalDespawn(() -> {
            Border border = Border.getInstance();
            Vec2 pos = getTransform().position;
            return pos.distance(border.getCenter()) > border.getRadius() + SPAWN_DISTANCE + 100;
        }));


        getTangible().onCollision.listen(phy -> {
            if (phy.entity.hasTag(GameTags.PLAYER)) {
                GameLoop.safeDestroy(entity);
                GameLoop.safeTrack(HealingOrb.makeEntity(getTransform().position, getEffect().getLevel() * 20));
                GameLoop.defer(() -> RandomPowerup.showScreen());
            }
        }, entity);
    }
    
}
