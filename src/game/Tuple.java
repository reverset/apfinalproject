package game;

public class Tuple<T, K> {
    public interface TupleConsumer<T, K> {
        void apply(T first, K second);

        default void apply(Tuple<T, K> tup) {
            apply(tup.first, tup.second);
        }
    }

    public final T first;
    public final K second;

    public Tuple(T first, K second) {
        this.first = first;
        this.second = second;
    }
}
