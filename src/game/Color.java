package game;

import com.raylib.Raylib;

public class Color {
	public static final Color WHITE = new ImmutableColor(255, 255, 255, 255);
	public static final Color RED = new ImmutableColor(255, 0, 0, 255);
	public static final Color BLUE = new ImmutableColor(0, 0, 255, 255);
	public static final Color GREEN = new ImmutableColor(0, 255, 0, 255);
	public static final Color AQUA = new ImmutableColor(0, 200, 255, 255);
	public static final Color GRAY = new ImmutableColor(25, 25, 25, 255);
	public static final Color PINK = new ImmutableColor(255, 105, 180, 255);
	public static final Color BLACK = new ImmutableColor(0, 0, 0, 255);
	public static final Color YELLOW = new ImmutableColor(255, 255, 0, 255);
	public static final Color ORANGE = new ImmutableColor(255, 140, 0, 255);
	public static final Color BLANK = new ImmutableColor(0, 0, 0, 0);

	public static final Color DARK_RED = new ImmutableColor(50, 0, 0, 255);
	public static final Color DARK_GREEN = new ImmutableColor(0, 50, 0, 255);
	public static final Color DARK_BLUE = new ImmutableColor(0, 0, 50, 255);

	public static final Color SPLASH_BLUE = new ImmutableColor(25, 25, 30, 255);
	
	public static final Color LIGHT_GRAY = new ImmutableColor(70, 70, 70, 255);
	
	private final Raylib.Color internal;
	
	public byte r;
	public byte g;
	public byte b;
	public byte a;
	
	public Color(Raylib.Color internal) {
		this(internal.r(), internal.g(), internal.b(), internal.a(), internal);
	}

	public Color(byte r, byte g, byte b, byte a, Raylib.Color internal) {
		this.internal = internal;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
		internal.r(r).g(g).b(b).a(a);
		
		// Janitor.registerAsyncSafe(this, internal::close);
	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}

	public boolean equals(Color obj) {
		return r == obj.r && g == obj.g && b == obj.b && a == obj.a;
	}
	
	public Color(byte r, byte g, byte b, byte a) {
		this(r, g, b, a, new Raylib.Color());
		
	}
	
	public Color(byte r, byte g, byte b) {
		this(r, g, b, (byte)255, new Raylib.Color());
		
	}
	
	public Color(int r, int g, int b) {
		this((byte)r,(byte)g,(byte)b);
	}
	
	public Color(int r, int g, int b, int a) {
		this((byte)r, (byte)g, (byte)b, (byte)a);
	}
	
	public Raylib.Color getPointer() {
		internal.r(r).g(g).b(b).a(a);
		return internal;
	}
	
	public Raylib.Color getPointerNoUpdate() {
		return internal;
	}

	public float magnitude() {
		return (float) Math.sqrt(r*r+g*g*b*b*a*a);
	}

	public float[] normalize() {
		float mag = magnitude();
		return new float[]{(r/mag), (g/mag), (b/mag), (a/mag)};
	}

	public Color clone() {
		return new Color(r, g, b, a);
	}

	public Color cloneIfImmutable() {
		if (this instanceof ImmutableColor) {
			return new Color(r, g, b, a);
		}
		return this;
	}

	public Color setAlpha(int alpha) {
		a = (byte)alpha;
		return this;
	}

	@Override
	public String toString() {
		return "Color(" + r + ", " + g + ", " + b + ", " + a + ")";
	}
}