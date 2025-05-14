package game.core;

import game.Color;
import game.RayImage;
import game.RayTexture;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class RhombusPowerup extends Powerup {

    private static RayTexture rhombus = new RayImage("resources/rhombus.png", 100, 100).uploadToGPU();
    private static Color tint = new Color(255, 255, 255, 100);
    private Transform trans;
    private Vec2 renderPosition = new Vec2();

    public RhombusPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Richmond Rhombus";
    }

    @Override
    public String getDescription() {
        return "A shield providing\nextra health";
    }

    @Override
    public void render() {
        renderPosition.setEq(trans.position.x - rhombus.width()/2, trans.position.y - rhombus.height()/2);
        rhombus.render(renderPosition, tint);
    }

    @Override
    public String getIconPath() {
        return "resources/rhombusicon.png";
    }
    
}
