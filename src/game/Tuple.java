package game;

public class Tuple<T, K> {
    public final T first;
    public final K second;

    public Tuple(T first, K second) {
        this.first = first;
        this.second = second;
    }
}
