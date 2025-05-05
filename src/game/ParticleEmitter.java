package game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class ParticleEmitter extends ECSystem {
    private RayTexture particleTexture;
    private float emitAngle;
    private float absMaxRandomSpread;
    private int maxParticles;
    private Duration nextParticleDuration;
    private Duration particleLifetime;

    private Consumer<Vec2> particleRenderer;
    private Function<Double, Double> sizeOverTime;

    private List<Vec2> particles = new ArrayList<>();

    public static EntityOf<ParticleEmitter> makeEntity(ParticleEmitter pe, Vec2 position, String name) {
        EntityOf<ParticleEmitter> e = new EntityOf<>(name, ParticleEmitter.class);

        e
            .addComponent(new Transform(position))
            .register(pe);

        return e;
    }

    public ParticleEmitter(
            RayTexture particleTexture, 
            float emitAngle, 
            float absMaxRandomSpread, 
            int maxParticles,
            Duration nextParticleDuration,
            Duration particleLifetime,
            Consumer<Vec2> particleRenderer,
            Function<Double, Double> sizeOverTime) {
        this.particleTexture = particleTexture;
        this.emitAngle = emitAngle;
        this.absMaxRandomSpread = absMaxRandomSpread;
        this.maxParticles = maxParticles;
        this.nextParticleDuration = nextParticleDuration;
        this.particleLifetime = particleLifetime;
        this.particleRenderer = particleRenderer;
        this.sizeOverTime = sizeOverTime;
    }

    @Override
    public void setup() {
    }
    

    @Override
    public void render() {
        
    }
}
