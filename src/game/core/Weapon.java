package game.core;

import game.ecs.Component;
import game.ecs.Entity;

public class Weapon implements Component {
    
    private int bulletsPerShot;
    private boolean burst;
    private int cooldown;

    public Weapon(int bulletsPerShot, int cooldown, boolean burst) {
        this.bulletsPerShot = bulletsPerShot;
        this.burst = burst;
        this.cooldown = cooldown;
    }

    public void fire() {

    }
}
