package game.core;

import java.util.List;

public class Round {
    public EnemySpawner spawner;
    private List<Wave> waves;
    private int currentWave = 0;

    public Round(List<Wave> waves, EnemySpawner spawner) {
        this.spawner = spawner;
        this.waves = waves;
        for (Wave wave : waves) {
            wave.spawner = spawner;
            wave.spawnQueue = spawner.getSpawnQueue();
        }
    }

    public Wave getWave() {
        return waves.get(currentWave);
    }

    public int getWaveIndex() {
        return currentWave;
    }

    public boolean update() {
        Wave wave = waves.get(currentWave);
        if (!wave.waveStarted) {
            wave.waveStarted = true;
            wave.start();
            return true;
        } else if (wave.isFinished()) {
            wave.waveStarted = false;
            currentWave += 1;
            if (currentWave >= waves.size()) {
                currentWave = 0;
            }
            if (wave instanceof BossWave bwave) {
                spawner.increaseLevel(bwave.getLevelValue());
            }
        } else {
            wave.update();
        }

        return false;
    }
}
