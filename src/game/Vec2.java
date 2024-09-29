package game;

import com.raylib.Raylib;

public class Vec2 {
	
	private final Raylib.Vector2 internal;
	
	public float x;
	public float y;
	
	public static Vec2 up() {
		return new Vec2(0, -1);
	}
	
	public static Vec2 down() {
		return new Vec2(0, 1);
	}
	
	public static Vec2 right() {
		return new Vec2(1, 0);
	}
	
	public static Vec2 left() {
		return new Vec2(-1, 0);
	}

	public static Vec2 zero() {
		return new Vec2();
	}

	public static Vec2 one() {
		return new Vec2(1, 1);
	}

	public static Vec2 screen() {
		return new Vec2(
			Raylib.GetScreenWidth(),
			Raylib.GetScreenHeight()
		);
	}

	public static Vec2 screenCenter() {
		return screen().multiplyEq(0.5f);
	}

	public static Vec2 random(Vec2 min, Vec2 max) {
		return new Vec2(
			(float) MoreMath.random(min.x, max.x), 
			(float) MoreMath.random(min.y, max.y)
		);
	}

	public static Vec2 randomUnit() {
		return new Vec2(
			(float) MoreMath.random(-1, 1),
			(float) MoreMath.random(-1, 1)
		).normalizeEq();
	}
	
	public Vec2(float x, float y, Raylib.Vector2 internal) {
		this.internal = internal;
		this.x = x;
		this.y = y;
		internal.x(x).y(y);
		
		// May have never been necessary to begin with
		// Janitor.registerAsyncSafe(this, internal::close);
	}

	public Vec2(Raylib.Vector2 internal) {
		this(internal.x(), internal.y(), internal);
		// this.internal = internal;
		// this.x = internal.x();
		// this.y = internal.y();
		// Janitor.register(this, internal::close);
	}
	
	public Vec2(float x, float y) {
		this(x, y, new Raylib.Vector2());
	}
	
	public Vec2() {
		this(0, 0);
	}
	
	public int xInt() {
		return (int)x;
	}
	public int yInt() {
		return (int)y;
	}
	
	public float dot(Vec2 other) {
		return x * other.x + y * other.y;
	}
	
	public float magnitude() {
		return (float) Math.sqrt(x*x + y*y);
	}

	public Vec2 negate() {
		return new Vec2(-x, -y);
	}

	public Vec2 negateEq() {
		x = -x;
		y = -y;
		return this;
	}
	
	public Vec2 normalize() {
		float mag = magnitude();
		if (mag == 0) {
			return Vec2.zero();
		}
		return divide(mag);
	}

	public Vec2 normalizeEq() {
		float mag = magnitude();
		if (mag == 0) {
			x = 0;
			y = 0;
			return this;
		}
		return divideEq(mag);
	}
	
	public Vec2 divide(Vec2 divisor) {
		return new Vec2(x / divisor.x, y / divisor.y);
	}
	
	public Vec2 divide(float divisor) {
		return new Vec2(x / divisor, y / divisor);
	}

	public Vec2 divideEq(float divisor) {
		x /= divisor;
		y /= divisor;
		return this;
	}

	public Vec2 divideEq(Vec2 divisor) {
		x /= divisor.x;
		y /= divisor.y;
		return this;
	}
	
	public Vec2 multiply(Vec2 other) {
		return new Vec2(x * other.x, y * other.y);
	}
	
	public Vec2 multiply(float scalar) {
		return new Vec2(x * scalar, y * scalar);
	}

	public Vec2 multiplyEq(float scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}
	
	public Vec2 add(Vec2 other) {
		return new Vec2(x + other.x, y + other.y);
	}

	public Vec2 add(float ox, float oy) {
		return new Vec2(x+ox, y+oy);
	}

	public Vec2 add(float scalar) {
		return new Vec2(x + scalar, y + scalar);
	}

	public Vec2 addEq(Vec2 other) {
		x += other.x;
		y += other.y;
		return this;
	}

	public Vec2 addEq(float ox, float oy) {
		x += ox;
		y += oy;
		return this;
	}

	public Vec2 minus(Vec2 other) {
		return new Vec2(x - other.x, y - other.y);
	}

	public Vec2 minus(float ox, float oy) {
		return new Vec2(x-ox, y-oy);
	}

	public Vec2 minus(float other) {
		return new Vec2(x - other, y - other);
	}

	public Vec2 minusEq(Vec2 other) {
		x -= other.x;
		y -= other.y;
		return this;
	}

	public Vec2 minusEq(float scalar) {
		x -= scalar;
		y -= scalar;
		return this;
	}

	public Vec2 minusEq(float ox, float oy) {
		x -= ox;
		y -= oy;
		return this;
	}

	public Vec2 clamp(Vec2 minimums, Vec2 maximums) {
		return new Vec2(
			MoreMath.clamp(x, minimums.x, maximums.x), 
			MoreMath.clamp(y, minimums.y, maximums.y));
	}

	public void clampEq(float minX, float minY, float maxX, float maxY) {
		x = MoreMath.clamp(x, minX, maxX);
		y = MoreMath.clamp(y, minY, maxY);
	}

	public Vec2 moveTowards(Vec2 other, float delta) {
		return new Vec2(
			MoreMath.moveTowards(x, other.x, delta),
			MoreMath.moveTowards(y, other.y, delta)
		);
	}

	public Vec2 roundEq() {
		x = Math.round(x);
		y = Math.round(y);
		return this;
	}

	public Vec2 roundEq(float place) {
		x = Math.round(x * place) / (float) place;
		y = Math.round(y * place) / (float) place;
		return this;
	}

	public Vec2 roundEq(float placeX, float placeY) {
		x = Math.round(x * placeX) / (float) placeX;
		y = Math.round(y * placeY) / (float) placeY;
		return this;
	}

	public Vec2 lerp(Vec2 other, float delta) {
		return new Vec2(MoreMath.lerp(x, other.x, delta), MoreMath.lerp(y, other.y, delta));
	}

	public Vec2 modulus(Vec2 other) {
		return new Vec2(Math.abs(x % other.x), Math.abs(y % other.y));
	}

	public Vec2 lerpEq(Vec2 other, float delta) {
		x = MoreMath.lerp(x, other.x, delta);
		y = MoreMath.lerp(y, other.y, delta);
		return this;
	}

	public void moveTowardsEq(Vec2 other, float delta) {
		x = MoreMath.moveTowards(x, other.x, delta);
		y = MoreMath.moveTowards(y, other.y, delta);
	}

	public Vec2 screenToWorld() {
		return new Vec2(Raylib.GetScreenToWorld2D(getPointer(), GameLoop.getMainCamera().getPointer()));
	}

	public Vec2 screenToWorldEq() {
		try (var v = Raylib.GetScreenToWorld2D(getPointer(), GameLoop.getMainCamera().getPointer())) {
			x = v.x();
			y = v.y();
		}
		return this;
	}

	public Vec2 directionTo(Vec2 other) {
		return (other.minus(this)).normalizeEq();
	}

	public float distance(Vec2 other) {
		// return (other.minus(this)).magnitude();
		return (float) Math.sqrt(Math.pow(other.x-x, 2) + Math.pow(other.y-y, 2));
	}

	public float getAngle() {
		return (float) Math.atan2(y, x);
	}

	public static Vec2 fromAngle(float angle) {
		return new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
	}

	public Vec2 clone() {
		return new Vec2(x, y);
	}

	public boolean is_approx(float x, float y, float epsilon) {
		return Math.abs(this.x - x) < epsilon 
				&& Math.abs(this.y - y) < epsilon;
	}

	public boolean isApprox(float x, float y) {
		final float EPSILON = 1e-7f;
		return is_approx(x, y, EPSILON);
	}

	@Override
	public String toString() {
		return "Vec2(" + x + ", " + y + ")";
	}
	
	public Raylib.Vector2 getPointer() {
		internal.x(x).y(y);
		return internal;
	}

	public Raylib.Vector2 getPointerNoUpdate() {
		return internal;
	}
}
