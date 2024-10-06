package game.core;

import java.util.List;
import java.util.Queue;

import game.EntityOf;

public class Round {
    private List<Wave> waves;
    private int currentWave = 0;

    public Round(List<Wave> waves, Queue<EntityOf<Enemy>> spawnQueue) {
        this.waves = waves;
        for (Wave wave : waves) {
            wave.spawnQueue = spawnQueue;
        }
    }

    public boolean update() { // true if wave changed.
        Wave wave = waves.get(currentWave);
        wave.update();
        
        if (wave.isFinished()) {
            currentWave += 1;
            if (currentWave >= waves.size()) {
                currentWave = 0;
            }
            return true;
        }
        return false;
    }
}
