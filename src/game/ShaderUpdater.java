package game;

import game.ecs.ECSystem;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.raylib.Raylib;

public class ShaderUpdater extends ECSystem {
    private Shader shader;

    private final List<Tuple<String, Supplier<Float>>> nameValueList;

    public ShaderUpdater(List<Tuple<String, Supplier<Float>>> nameValue) {
        nameValueList = nameValue;
    }

    public ShaderUpdater(Tuple<String, Supplier<Float>>[] nameValue) {
        this(Arrays.asList(nameValue));
    }

    @Override
    public void setup() {
        shader = require(Shader.class);
    }

    @Override
    public void frame() {
        nameValueList.forEach((tup) -> shader.setShaderValue(tup.first, tup.second.get()));
    }
    
    
    public static Tuple<String, Supplier<Float>> timeUpdater() {
        Supplier<Float> sup = () -> (float) Raylib.GetTime();
        return new Tuple<String, Supplier<Float>>("time", sup);
    }
}
