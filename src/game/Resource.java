package game;

public interface Resource {
    void init();
    void deinit();
    String getResourcePath();
    boolean isLoaded();
}
