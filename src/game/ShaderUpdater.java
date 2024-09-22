package game;

import game.ecs.ECSystem;

import java.util.List;
import java.util.function.Supplier;

public class ShaderUpdater extends ECSystem {
    private Shader shader;

    private final List<Tuple<String, Supplier<Float>>> nameValueList;

    public ShaderUpdater(List<Tuple<String, Supplier<Float>>> nameValue) {
        nameValueList = nameValue;
    }

    @Override
    public void setup() {
        shader = require(Shader.class);
    }

    @Override
    public void frame() {
        nameValueList.forEach((tup) -> shader.setShaderValue(tup.first, tup.second.get()));
    }
    
}
