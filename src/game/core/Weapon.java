package game.core;

import game.ecs.Component;

public class Weapon implements Component {
    
    private int bulletsPerShot;
    private boolean burst;

    public Weapon(int bulletsPerShot, boolean burst) {
        this.bulletsPerShot = bulletsPerShot;
        this.burst = burst;
    }
}
