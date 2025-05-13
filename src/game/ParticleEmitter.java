package game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import game.ecs.ECSystem;
import game.ecs.comps.Transform;

public class ParticleEmitter extends ECSystem {
    private float emitAngle;
    private float angleVariation;
    private int maxParticles;
    private Duration nextParticleDuration;
    private Duration particleLifetime;

    private Consumer<Particle> particleRenderer;
    private Function<Double, Double> sizeOverTime;
    private Function<Double, Double> speedOverTime;

    private Stopwatch spawnStopwatch = Stopwatch.ofGameTime();

    private List<Particle> particles = new ArrayList<>();
    private Transform trans;
    private boolean relativePosition;
    private float spawnRadius;
    private SpawnStrategy spawnStrategy;

    public enum SpawnStrategy {
        PERIMETER,
        AREA
    }

    public class Particle {
        private Vec2 position = new Vec2();
        private Vec2 realPosition = new Vec2();
        private Vec2 velocity = new Vec2();
        private double size = 1;
        private double spawnTime = GameLoop.getUnpausedTime();
        private double timeAlive = 0;
        private double lifetime = 0;

        public double getLifetime() {
            return lifetime;
        }

        public double getTimeAlive() {
            return timeAlive;
        }

        public double getSize() {
            return size;
        }

        public Vec2 getPosition() {
            return position;
        }

        public Vec2 getVelocity() {
            return velocity;
        }

        public void setVelocity(Vec2 velocity) {
            this.velocity = velocity;
        }

        public void setPosition(Vec2 pos) {
            position = pos;
        }

        public void setSize(double size) {
            this.size = size;
        }

        public double getSpawnTime() {
            return spawnTime;
        }

        public Vec2 getRealPosition() {
            if (relativePosition) {
                realPosition.setEq(position.x + trans.position.x, position.y + trans.position.y);
                return realPosition;
            }
            return position;
        }
    }

    public static EntityOf<ParticleEmitter> makeEntity(ParticleEmitter pe, Vec2 position, String name) {
        EntityOf<ParticleEmitter> e = new EntityOf<>(name, ParticleEmitter.class);

        e
            .addComponent(new Transform(position))
            .register(pe);

        return e;
    }

    public ParticleEmitter(
            float emitAngle, 
            float angleVariation, 
            int maxParticles,
            Duration nextParticleDuration,
            Duration particleLifetime,
            boolean relativePosition,
            float spawnRadius,
            SpawnStrategy spawnStrategy,
            Consumer<Particle> particleRenderer,
            Function<Double, Double> sizeOverTime,
            Function<Double, Double> speedOverTime) {
        this.emitAngle = emitAngle;
        this.angleVariation = angleVariation;
        this.maxParticles = maxParticles;
        this.nextParticleDuration = nextParticleDuration;
        this.particleLifetime = particleLifetime;
        this.relativePosition = relativePosition;
        this.spawnRadius = spawnRadius;
        this.spawnStrategy = spawnStrategy;
        this.particleRenderer = particleRenderer;
        this.sizeOverTime = sizeOverTime;
        this.speedOverTime = speedOverTime;
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
    }
    
    @Override
    public void frame() {
        if (particles.size() <= maxParticles && spawnStopwatch.hasElapsedAdvance(nextParticleDuration)) {
            Particle particle = new Particle();
            particle.lifetime = particleLifetime.toMillis() / 1_000.0;

            if (!relativePosition) {
                particle.getPosition().setEq(trans.position.x, trans.position.y);
            }
            if (spawnStrategy == SpawnStrategy.PERIMETER) particle.getPosition().addRandomByCoeffEq(spawnRadius);
            else if (spawnStrategy == SpawnStrategy.AREA) particle.getPosition().addRandomByCoeffEq(-spawnRadius, spawnRadius);

            particle.getVelocity()
                .setFromAngleEq((float)(emitAngle + MoreMath.randomExcluding(-angleVariation, angleVariation)))
                .multiplyEq((speedOverTime.apply(particle.timeAlive).floatValue()));
            particles.add(particle);
        }

        for (int i = particles.size()-1; i >= 0; i--) {
            if (GameLoop.getUnpausedTime() > particles.get(i).getSpawnTime() + particleLifetime.toSeconds()) {
                particles.remove(i);
                continue;
            }
            Particle particle = particles.get(i);

            particle.timeAlive = GameLoop.getUnpausedTime() - particle.spawnTime;

            Vec2 dV = particle.velocity.multiply(delta());
            particle.position.addEq(dV);

            particle.setSize(sizeOverTime.apply(particle.timeAlive));
        }
    }

    @Override
    public void render() {
        for (Particle particle : particles) {
            particleRenderer.accept(particle);
        }
    }
}
