package game;

import game.ecs.Component;
import com.raylib.Raylib;

import java.util.HashMap;
import org.bytedeco.javacpp.FloatPointer;

import com.raylib.Jaylib;

public class Shader implements Component {
    public static final int UNIFORM_FLOAT = 0;

    private final Raylib.Shader internal;  

    private final HashMap<String, Integer> fieldMap = new HashMap<>();
    
    public Shader(String path) {
        internal = Jaylib.LoadShader("resources/default.vert", path);
        Janitor.register(this, () -> Raylib.UnloadShader(internal));
    }

    public Shader setShaderValue(String name, float value) {
        int loc = fieldMap.getOrDefault(name, -1);
        if (loc == -1) {
            loc = Raylib.GetShaderLocation(internal, name);
            if (loc == -1) {
                throw new RuntimeException("Shader has no field '" + name + "'.");
            }
            fieldMap.put(name, loc);
        }

        Raylib.SetShaderValue(internal, loc, new FloatPointer(new float[]{value}), UNIFORM_FLOAT);
        return this;
    }

    public void activate() {
        Raylib.BeginShaderMode(internal);
    }

    public void deactivate() {
        Raylib.EndShaderMode();
    }

    public Raylib.Shader getPointer() {
        return internal;
    }
}
