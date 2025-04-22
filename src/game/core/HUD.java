package game.core;

import java.util.List;

import com.raylib.Raylib;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.ecs.ECSystem;

// Not the enitre HUD lol.
public class HUD extends ECSystem {

    private final Player player;
    private List<Powerup> powerupsCache = List.of();

    public static EntityOf<HUD> makeEntity(Player player) {
        EntityOf<HUD> e = new EntityOf<>("hud", HUD.class);

        e.register(new HUD(player));

        return e;
    }

    private HUD(Player player) {
        this.player = player;
    }

    @Override
    public void setup() {
    }
    
    @Override
    public void infrequentUpdate() {
        powerupsCache = player.getEffect().getPowerups();
    }

    @Override
    public void hudRender() {
        
        final int fontSize = 24;
        final int smallFontSize = 20;
        int y = GameLoop.SCREEN_HEIGHT / 2 - powerupsCache.size() * fontSize;

        for (final var powerup : powerupsCache) {
            Raylib.DrawText(powerup.getName() + " Lv" + powerup.level, 15, y, fontSize, Color.WHITE.getPointerNoUpdate());
            Raylib.DrawText(powerup.getSmallHUDInfo(), 15, y+24, smallFontSize, Color.GREEN.getPointerNoUpdate());
            y += fontSize * 2;
        }
    }
    
}
