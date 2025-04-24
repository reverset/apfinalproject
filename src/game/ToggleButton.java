package game;

public class ToggleButton extends BetterButton {
    public final Signal<Void> onEnabled = new Signal<>();
    public final Signal<Void> onDisable = new Signal<>();
    public final Signal<Boolean> onToggle = new Signal<>();
    
    private boolean state;

    private static final Color ENABLED = Color.DARK_GREEN;
    private static final Color DISABLED = Color.GRAY;

    public ToggleButton(boolean defaultState, Color outlineColor, int roundness, int segments) {
        super(outlineColor, defaultState ? ENABLED : DISABLED, roundness, segments);
        state = defaultState;

        onClick.listen(n -> {
            if (state) onDisable.emit(null);
            else onEnabled.emit(null);
            state = !state;
            onToggle.emit(state);

            setColor(state ? ENABLED : DISABLED);
        });
    }

    public boolean isToggled() {
        return state;
    }
}
