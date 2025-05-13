package game;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.raylib.Raylib;

import game.ParticleEmitter.SpawnStrategy;

public class ParticlePresets {

    private static Consumer<ParticleEmitter.Particle> genericSquareRenderer(Supplier<Color> color) {
        return genericSquareRenderer(i -> color.get());
    }

    private static Consumer<ParticleEmitter.Particle> genericSquareRenderer(Function<ParticleEmitter.Particle, Color> color) {
        return particle -> {
            int size = (int)(particle.getSize() * 10);
            int halfSize = size / 2;
            Raylib.DrawRectangle(particle.getRealPosition().xInt() - halfSize, particle.getRealPosition().yInt() - halfSize, size, size, color.apply(particle).getPointer());
        };
    }

    private static Function<ParticleEmitter.Particle, Color> fadeInOut(Color color) {
        final Color desiredColor = color.cloneIfImmutable();
        return particle -> {
            double lifeRatio = particle.getTimeAlive() / particle.getLifetime();
            if (lifeRatio <= 0.33) {
                return desiredColor.setAlpha((int) ((particle.getTimeAlive() / (particle.getLifetime() * 0.33)) * 255));
            } else if (lifeRatio >= 0.66) {
                return desiredColor.setAlpha((int) (255 - (particle.getTimeAlive() * 0.66) / (particle.getLifetime() * 0.33) * 255));
            }
            return color;
        };
    }


    public static ParticleEmitter flame(Color color) {
        return new ParticleEmitter((float)(MoreMath.TAU * 0.75), (float)(0.3f), 1000,
            Duration.ofMillis(10), Duration.ofSeconds(1), 
            false, 10, SpawnStrategy.AREA,
            genericSquareRenderer(() -> color), 
            time -> (time+1)/2, time -> (double)300);
    }

    public static ParticleEmitter dust() {
        return new ParticleEmitter(0, (float)MoreMath.TAU, 300, 
            Duration.ofMillis(100), Duration.ofSeconds(5), 
            false, 1_000, SpawnStrategy.AREA,
            genericSquareRenderer(fadeInOut(Color.LIGHT_GRAY)), 
            time -> 1.0, time -> 50.0);
    }
}
