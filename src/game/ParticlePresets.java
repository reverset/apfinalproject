package game;

import java.time.Duration;

import com.raylib.Raylib;

public class ParticlePresets {
    public static ParticleEmitter flame(Color color) {
        return new ParticleEmitter((float)(MoreMath.TAU * 0.75), (float)(0.3f), 1000, Duration.ofMillis(10), Duration.ofSeconds(1), false, 300, particle -> {
            int size = (int)(particle.getSize() * 10);
            int halfSize = size / 2;
            Raylib.DrawRectangle(particle.getRealPosition().xInt() - halfSize, particle.getRealPosition().yInt() - halfSize, size, size, color.getPointer());
        }, time -> (time+1)/2, time -> (double)300);
    }
}
