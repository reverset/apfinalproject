package game;

// I really don't want my game to crash, even if something does go really wrong
// So whenever I need to throw, I throw this exception instead
// So that the game loop can catch it and try to continue as normal.
public class RecoverableException extends RuntimeException {
    public RecoverableException() {}
    public RecoverableException(String msg) {
        super(msg);
    }
}
