package game;

import game.ecs.Component;
import com.raylib.Raylib;

import java.util.HashMap;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;

import com.raylib.Jaylib;

public class Shader implements Component {
    public static final int UNIFORM_FLOAT = 0;
    public static final int UNIFORM_VEC2 = 1;
    public static final int UNIFORM_VEC3 = 2;
    public static final int UNIFORM_VEC4 = 3;
    public static final int UNIFORM_INT = 4;

    private final Raylib.Shader internal;  

    private final HashMap<String, Integer> fieldMap = new HashMap<>();
    private Runnable resetFunction = () -> {};
    
    public Shader(String path) {
        internal = Jaylib.LoadShader("resources/default.vert", path);
        Janitor.register(this, () -> Raylib.UnloadShader(internal));
    }

    public Shader setShaderValue(String name, float[] value) {
        if (value.length != 3) {
            throw new IllegalArgumentException("value param must have 3 elements.");
        }
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new FloatPointer(value), UNIFORM_VEC3);
        return this;
    }

    public Shader setShaderValue(String name, float value) {
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new FloatPointer(new float[]{value}), UNIFORM_FLOAT);
        return this;
    }

    public Shader setShaderValue(String name, int value) {
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new IntPointer(new int[]{value}), UNIFORM_INT);
        return this;
    }

    public Shader setShaderValue(String name, boolean value) {
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new IntPointer(new int[]{value ? 1 : 0}), UNIFORM_INT);
        return this;
    }

    public Shader setShaderValue(String name, Color value) { // UNTESTED
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new FloatPointer(new float[]{value.r, value.g, value.b, value.a}), UNIFORM_VEC4);
        return this;
    }

    public Shader setShaderValue(String name, Vec2 value) {
        int loc = getLocation(name);

        Raylib.SetShaderValue(internal, loc, new FloatPointer(new float[]{value.x, value.y}), UNIFORM_VEC2);
        return this;
    }

    public Shader setResetFunction(Runnable action) {
        resetFunction = action;
        return this;
    }

    public void reset() {
        resetFunction.run();
    }

    private int getLocation(String name) {
        int loc = fieldMap.getOrDefault(name, -1);
        if (loc == -1) {
            loc = Raylib.GetShaderLocation(internal, name);
            if (loc == -1) {
                throw new RuntimeException("Shader has no field '" + name + "'.");
            }
            fieldMap.put(name, loc);
        }
        return loc;
    }

    public void activate() {
        Raylib.BeginShaderMode(internal);
    }

    public void deactivate() {
        Raylib.EndShaderMode();
    }

    public void with(Runnable action) {
        try {
            activate();
            action.run();
        } finally {
            deactivate();
        }
    }

    public Raylib.Shader getPointer() {
        return internal;
    }
}
