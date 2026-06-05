package smartin.miapi.modules.abilities.key.handler;

public final class BindingState {
    public boolean pressed = false;
    public boolean lastPressed = false;
    public int holdTicks = 0;
    public long lastPressTime = 0;
    public boolean clientRegistered = false;
    public long lastReleaseTime = 0;
}