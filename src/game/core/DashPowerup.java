package game.core;

import java.time.Duration;

import com.raylib.Raylib;

import game.Color;
import game.GameLoop;
import game.ParticlePresets;
import game.Stopwatch;
import game.Tween;
import game.Vec2;
import game.ecs.Entity;
import game.ecs.RequireException;
import game.ecs.comps.Transform;

public class DashPowerup extends Powerup {
    private boolean canDash = true;
    private Stopwatch dashReset = Stopwatch.ofGameTime();
    private Duration dashCooldown = Duration.ofSeconds(5);
    private double resetTimestamp = -1;

    private float dashMagnitude = 200;
    
    private Tangible tangible;
    private Transform trans;
    private Health health;
    private Controllable control;

    public DashPowerup(Entity entity, Weapon2 weapon, Effect effect, int level) {
        super(entity, weapon, effect, level);
    }

    @Override
    public void setup() {
        super.setup();
        tangible = require(Tangible.class);
        trans = require(Transform.class);
        health = require(Health.class);
        
        final var iter = entity.systemIterator();
        boolean found = false;
        while (iter.hasNext()) {
            final var sys = iter.next();
            if (sys instanceof Controllable c) {
                control = c;
                found = true;

                break;
            }
        }
        if (!found) {
            throw new RequireException("missing Controllable system");
        }
    }

    @Override
    public void infrequentUpdate() {
        if (!canDash && dashReset.hasElapsedAdvance(dashCooldown)) {
            canDash = true;
        }
    }

    @Override
    public void frame() {
        super.frame();
        if (canDash && Raylib.IsKeyPressed(Raylib.KEY_SPACE)) {
            if (tangible.velocity.isApprox(0, 0)) return;

            canDash = false;
            resetTimestamp = GameLoop.getUnpausedTime() + dashCooldown.toSeconds();
            dashReset.restart();

            Vec2 dir = control.controlledMoveVector();
            Vec2 target = trans.position.add(dir.clone().multiplyEq(dashMagnitude));
            tangible.velocity.setEq(dir.x, dir.y).multiplyEq(dashMagnitude); // will get clamped by player

            health.setInvincible(true);

            GameLoop.makeTweenGameTime(Tween.lerp(trans.position.clone(), target), 0.1, val -> {
                trans.position.setEq(val.x, val.y);
            }).start().onFinish.listenOnce(n -> health.setInvincible(false)); // if the player was invincible by other means, this would override it, however this is the only situation in which they would be invincible so it's fine, but its something to consider.

            GameLoop.makeTemporary(Duration.ofSeconds(1), trans.position.clone(), ParticlePresets.pop(10, Color.GREEN));
            
        }
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    protected void doLevelUp() {
        level += 1;
    }

    @Override
    public String getName() {
        return "Dash";
    }

    @Override
    public String getDescription() {
        return "Dash to avoid attacks";
    }

    @Override
    public String getSmallHUDInfo() {
        if (canDash) {
            return "Press Space to Dash!";
        }
        return (int)(Math.ceil(resetTimestamp - GameLoop.getUnpausedTime())) + " seconds";
    }

    @Override
    public String getIconPath() {
        return "resources/dash.png";
    }
}
