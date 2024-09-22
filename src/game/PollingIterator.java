package game;

import java.util.Queue;

public class PollingIterator<T> implements java.util.Iterator<T> {

    private final Queue<T> queue;

    public PollingIterator(Queue<T> queue) {
        this.queue = queue;
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public T next() {
        return queue.poll();
    }
    
}
