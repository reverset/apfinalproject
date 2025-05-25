package game.core;

import game.Signal;

public class Settings {
    public static boolean dynamicZoom = true; // i should have getters & setters for all of these but lazy.
    public static boolean cameraShake = true;
    public static boolean dust = true;
    public static boolean crosshairEnabled = true;

    public static final Signal<Boolean> onMusicEnableChange = new Signal<>();

    private static boolean musicEnabled = true;

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void setMusicEnabled(boolean enabled) {
        if (musicEnabled != enabled) {
            onMusicEnableChange.emit(enabled);
        }
        musicEnabled = enabled;
    }
}
