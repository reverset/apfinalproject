package game;

import java.lang.ref.Cleaner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Janitor { // Raylib is single-threaded. So make sure to free on the main thread.
	private static final Cleaner CLEANER = Cleaner.create();
	
	private static final ConcurrentLinkedQueue<Runnable> cleanupQueue = new ConcurrentLinkedQueue<>();
	
	public static void register(Object obj, Runnable action) {
		CLEANER.register(obj, () -> {
			cleanupQueue.add(action);
			// System.out.println("Queuing a cleanup action...");
		});
	}

	public static void registerAsyncSafe(Object obj, Runnable action) {
		CLEANER.register(obj, action);
	}
	
	public static Runnable poll() {
		return cleanupQueue.poll();
	}
}
