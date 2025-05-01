package game.core;

import java.time.Duration;
import java.util.Optional;

import game.Color;
import game.GameLoop;
import game.RayImage;
import game.RayTexture;
import game.Stopwatch;
import game.Vec2;
import game.core.rendering.Poly;
import game.core.rendering.Rect;
import game.core.rendering.TextureRenderer;
import game.ecs.Entity;
import game.ecs.comps.Transform;

public class HexaBomb extends Bullet {
    public static final float RADIUS = 15;
    public static final float SPEED = 10;
    public static final float FRICTION_COEFF = 200;

    public static final float DEGREE_PER_PELLET = 15;
    public static final float PELLET_SPEED = 600;

    public static final Duration LIFETIME = Duration.ofSeconds(2);
    public static final Duration PELLET_LIFETIME = Duration.ofSeconds(3);
    public static final float CHARGEUP = 1;

    private static final RayTexture WARNING_INDICATOR = new RayImage("resources/warningindicator.png", 50, 50).uploadToGPU();

    private Duration lifetime;
    private Stopwatch detonationStopwatch = Stopwatch.ofGameTime();
    private Stopwatch warningBlinkStopwatch = Stopwatch.ofGameTime();

    private NovaWeapon detonation;

    private LaserWeapon altDetonation;
    private LaserWeapon altDetonation2;
    private LaserWeapon altDetonation3;
    private LaserWeapon altDetonation4;

    private LaserWeapon altDetonation5;
    private LaserWeapon altDetonation6;
    private LaserWeapon altDetonation7;
    private LaserWeapon altDetonation8;

    private Color color;
    private boolean isLaserVarient = Math.random() >= 0.5;

    private TextureRenderer warningIndicatorRenderer;

    public HexaBomb(Duration lifetime, Optional<Effect> effect, Entity owner, int damage, Object[] ignoreTags, Color color, Weapon2 weapon) {
        super(owner, damage, effect, ignoreTags, weapon);
        this.lifetime = lifetime;
        this.color = color.cloneIfImmutable();
    }

    @Override
    public void setup() {
        trans = require(Transform.class);
        tangible = require(Tangible.class);
        rect = require(Rect.class);

        Poly poly = require(Poly.class);

        
        if (isLaserVarient) {
            altDetonation = new LaserWeapon(damage*2, trans.position, Vec2.right(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation2 = new LaserWeapon(damage*2, trans.position, Vec2.left(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation3 = new LaserWeapon(damage*2, trans.position, Vec2.up(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation4 = new LaserWeapon(damage*2, trans.position, Vec2.down(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            
            altDetonation5 = new LaserWeapon(damage*2, trans.position, Vec2.ne(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation6 = new LaserWeapon(damage*2, trans.position, Vec2.nw(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation7 = new LaserWeapon(damage*2, trans.position, Vec2.sw(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
            altDetonation8 = new LaserWeapon(damage*2, trans.position, Vec2.se(), CHARGEUP, Color.YELLOW, 2_000, 1_000, 15, 0, GameTags.ENEMY_TEAM_TAGS, 0, effect);
        } else {
            detonation = new NovaWeapon(damage, DEGREE_PER_PELLET, PELLET_SPEED, color, ignoreTags, 0, PELLET_LIFETIME, effect);
            warningIndicatorRenderer = requireOrAddSystem(TextureRenderer.class, () -> new TextureRenderer(WARNING_INDICATOR));
        }
        
    }
    
    @Override
    public void ready() {
        detonationStopwatch.start();
    }

    @Override
    public void frame() {
        
    }

    @Override
    public void infrequentUpdate() {
        tangible.velocity.moveTowardsEq(Vec2.ZERO, FRICTION_COEFF*infreqDelta());
        
        if (isLaserVarient) {
            if (detonationStopwatch.hasElapsedAdvance(lifetime) && !altDetonation.isCharging()) {
                altDetonation.chargeUp(() -> trans.position, Vec2::right, entity, b -> {}).onFinish.listen((n) -> {
                    GameLoop.runAfter(entity, Duration.ofMillis(500), () -> GameLoop.safeDestroy(entity));
                }, entity);
        
                altDetonation2.chargeUp(() -> trans.position, Vec2::up, entity, b -> {});
                altDetonation3.chargeUp(() -> trans.position, Vec2::left, entity, b -> {});
                altDetonation4.chargeUp(() -> trans.position, Vec2::down, entity, b -> {});
        
                altDetonation5.chargeUp(() -> trans.position, Vec2::ne, entity, b -> {});
                altDetonation6.chargeUp(() -> trans.position, Vec2::nw, entity, b -> {});
                altDetonation7.chargeUp(() -> trans.position, Vec2::sw, entity, b -> {});
                altDetonation8.chargeUp(() -> trans.position, Vec2::se, entity, b -> {});
            }
        } else {
            if (detonationStopwatch.hasElapsedAdvance(lifetime)) {
                GameLoop.safeDestroy(entity);
                detonation.forceFire(trans.position, null, entity);
            }

            if (warningBlinkStopwatch.hasElapsedSecondsAdvance(0.1)) {
                warningIndicatorRenderer.setEnabled(!warningIndicatorRenderer.isEnabled());
            }
        }
    }

    @Override
    public void render() {
        if (isLaserVarient) {
            altDetonation.render();
            altDetonation2.render();
            altDetonation3.render();
            altDetonation4.render();
    
            altDetonation5.render();
            altDetonation6.render();
            altDetonation7.render();
            altDetonation8.render();
        }
    }
    
}
