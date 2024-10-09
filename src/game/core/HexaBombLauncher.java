package game.core;

import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.Vec2;
import game.ecs.Entity;

public class HexaBombLauncher extends Weapon2 {

    private Object[] ignoreTags;
    private Color color;
    private float speed;
    private int damage;

    public HexaBombLauncher(int damage, float speed, Color color, Object[] ignoreTags, float cooldown, Optional<Effect> effect) {
        super(cooldown, effect);
        this.damage = damage;
        this.speed = speed;
        this.color = color;
        this.ignoreTags = ignoreTags;

    }

    @Override
    void forceFire(Vec2 position, Vec2 direction, Entity owner) {
        EntityOf<HexaBomb> hexaBomb = BulletFactory.hexaBomb(damage, effect, position, direction.multiply(speed), color, owner, ignoreTags, HexaBomb.LIFETIME);

        GameLoop.safeTrack(hexaBomb);
    }
    
}
