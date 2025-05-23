package game.core;

import java.time.Duration;

import com.raylib.Raylib;

import game.Color;
import game.GameLoop;
import game.Tween;
import game.Vec2;
import game.ecs.ECSystem;

public class StartTip extends ECSystem {
    private static final String MESSAGE = "Use WASD to move. Click to toggle shooting. Aim with your mouse.";

    private String revealed = "";

    public static void spawn() {
        GameLoop.makeTemporary(Duration.ofSeconds(6), new Vec2(), new StartTip());
    }

    @Override
    public void setup() {
    }

    @Override
    public void ready() {
        GameLoop.makeTweenGameTime(Tween.reveal(MESSAGE), 1, val -> {
            revealed = val;
        }).start();
    }

    @Override
    public void hudRender() {
        Raylib.DrawText(revealed, 100, GameLoop.SCREEN_HEIGHT - 200, 34, Color.WHITE.getPointerNoUpdate());
    }
}
