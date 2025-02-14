package game;

import java.util.function.Supplier;

@FunctionalInterface
public interface FloatSupplier {
    float getAsFloat();

    default Supplier<Float> boxed() {
        return this::getAsFloat;
    }
}
