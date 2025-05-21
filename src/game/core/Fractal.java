package game.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import game.Color;
import game.EntityOf;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.RecoverableException;
import game.Stopwatch;
import game.Tween;
import game.Vec2;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.comps.Transform;

public class Fractal extends Unit {

    private static final RayTexture texture = new RayImage("resources/fractal.png", 512, 512).uploadToGPU();

    private static final int BASE_HEXABOMB_DAMAGE = 5;
    private static final int BASE_HORIZONTAL_LASER_DAMAGE = 8;
    private static final int BASE_ARC_WEAPON_DAMAGE = 5;
    private static final int BASE_HEALTH = 500; // first encounter is at level 31. so 500 * 31 = 15,500
    private static final int MAX_TRIANGLES = 3;

    private Optional<Vec2> desiredPosition = Optional.empty();
    private Stopwatch movementStopwatch = Stopwatch.ofGameTime();
    private Stopwatch triangleSpawn = Stopwatch.ofGameTime();
    private Stopwatch hexabombShoot = Stopwatch.ofGameTime();
    private Stopwatch circleSpawn = Stopwatch.ofGameTime();
    private Stopwatch sigmaSpawn = Stopwatch.ofGameTime();
    private Stopwatch horizontalLasersShoot = Stopwatch.ofGameTime();
    private Stopwatch arcWeaponShoot = Stopwatch.ofGameTime();

    private Unit player = null;

    private int triangles = 0;
    private float originalBorderRadius = 1_000;

    private HexaBombLauncher hexaBombLauncher;

    private List<LaserWeapon> horizontalLasers = new ArrayList<>();
    private ArcWeapon arcWeapon;

    public static EntityOf<Fractal> makeEntity(Vec2 pos, int level) {
        EntityOf<Fractal> e = new EntityOf<>("The Mandelbrot Fractal", Fractal.class);
        
        e
            .addComponent(new Transform(pos))
            .addComponent(new Tangible())
            .addComponent(new Effect().setLevel(level))
            .addComponent(new Health(BASE_HEALTH * level))
            .addComponent(new Rect(texture.width()/2, texture.height()/2, Color.RED))
            .register(new Physics(0, 0, new Vec2(-texture.width()/4, -texture.height()/4)))
            .register(new HealthBar(new Vec2(), e.name, true))
            .register(new TextureRenderer(texture))
            .register(new Fractal())
            .addTags(GameTags.ENEMY_TEAM_TAGS);

        return e;
    }

    @Override
    public void setup() {
        getEffect().addDamageScaling(info -> info.damage()*getEffect().getLevel());

        getHealth().onDeath.listenOnce(n -> {
            GameLoop.safeDestroy(entity);
            Border.getInstance().setRadius(originalBorderRadius);
            GameLoop.defer(() -> RandomPowerup.showScreen());
        });

        getHealth().onDamage.listen(info -> {
            // if (getHealth().getHealthPercentage() < 0.7) {
            //     GameLoop.makeTemporary(Duration.ofSeconds(3), getTransform().position, ParticlePresets.pop(60, Color.RED));
            // }
        }, entity);

        hexaBombLauncher = new HexaBombLauncher(BASE_HEXABOMB_DAMAGE, 300, Color.YELLOW, GameTags.ENEMY_TEAM_TAGS, 1, Optional.of(getEffect()));
    
        for (int i = 0; i < 10; i++) {
            horizontalLasers.add(new LaserWeapon(BASE_HORIZONTAL_LASER_DAMAGE, new Vec2(), new Vec2(), 1, Color.PINK, 4_000, 1000, 10, 0, GameTags.ENEMY_TEAM_TAGS, 1, Optional.of(getEffect())));
        }

        arcWeapon = new ArcWeapon(BASE_ARC_WEAPON_DAMAGE, (float)Math.toRadians(45), 20, 300, Color.RED, GameTags.ENEMY_TEAM_TAGS, 1, Duration.ofSeconds(2), Optional.of(getEffect()));
    }

    @Override
    public void ready() {
        super.ready();
        getHealth().setInvincible(true);
        GameLoop.makeTweenGameTime(Tween.lerp(10, getHealth().getMaxHealth()), 3, val -> {
            getHealth().setHealth(val.intValue());
        }).start().onFinish.listenOnce(n -> getHealth().setInvincible(false));

        if (player == null) player = getTeam().findTarget(getTransform().position).orElseThrow(() -> new RecoverableException("missing player"));
        
        originalBorderRadius = Border.getInstance().getRadius();

        GameLoop.makeTweenGameTime(Tween.lerp(Border.getInstance().getRadius(), 400), 5, val -> {
            Border.getInstance().setRadius(val);
        }).start();
    }

    @Override
    public void infrequentUpdate() {
        if (movementStopwatch.hasElapsedAdvance(Duration.ofSeconds(1))) {
            desiredPosition = Optional.of(player.getTransform().position.addRandomByCoeff(500));
        }

        if (triangles < MAX_TRIANGLES && triangleSpawn.hasElapsedAdvance(Duration.ofSeconds(1))) {
            EntityOf<Unit> tri = GameLoop.safeTrack(TriangleEnemy.makeEntity(getTransform().position.clone(), getEffect().getLevel()));
            tri.getMainSystem().getHealth().onDeath.listenOnce(n -> triangles -= 1);
            triangles += 1;
        }

        if (hexabombShoot.hasElapsedAdvance(Duration.ofSeconds(4))) {
            hexaBombLauncher.fire(getTransform().position.clone(), getTransform().position.directionTo(player.getTransform().position), entity);
        }

        if (circleSpawn.hasElapsedSecondsAdvance(16)) {
            GameLoop.safeTrack(CircleEnemy.makeEntity(getTransform().position.clone(), getEffect().getLevel()));
        }

        if (sigmaSpawn.hasElapsedSecondsAdvance(8)) {
            GameLoop.safeTrack(Sigma.makeEntity(getTransform().position.clone(), new Vec2(), getEffect().getLevel()));
        }

        if (horizontalLasersShoot.hasElapsedSecondsAdvance(10)) {
            Vec2 center = Border.getInstance().getCenter();
            
            for (int i = 0; i < horizontalLasers.size(); i++) {
                int j = i;
                GameLoop.runAfterGameTime(entity, Duration.ofMillis(i*100), () -> {
                    LaserWeapon l = horizontalLasers.get(j);
                    Vec2 pos = new Vec2(center.x - 2_000, center.y + 500 - (j * 100));
                    l.chargeUp(() -> pos, () -> Vec2.right(), entity, impending -> {});
                });
            }
        }

        if (arcWeaponShoot.hasElapsedSecondsAdvance(5)) {
            arcWeapon.fire(getTransform().position, getTransform().position.directionTo(player.getTransform().position), entity);
        }
    }
    
    @Override
    public void frame() {
        getTangible().velocity.setEq(0, 0);
        getTransform().rotation = 180 + getTransform().position.directionTo(player.getTransform().position).getAngleDegrees();

        desiredPosition.ifPresent(p -> {
            getTransform().position.moveTowardsEq(p, 500 * delta());
        });

        Vec2 desiredBorderCenter = Vec2.fromAngle((float)GameLoop.getUnpausedTime() / 8).multiplyEq(500);
        Border.getInstance().getCenter().moveTowardsEq(desiredBorderCenter, 1000 * delta());
    }

    @Override
    public boolean isBossEnemy() {
        return true;
    }

    @Override
    public void render() {
        horizontalLasers.forEach(LaserWeapon::render);
    }
}
