package smartin.miapi.client.gui.state;

/**
 * meant as a lifecicle tracker to prevent State logic from keeping track of unneccesary stuff
 */
public interface UiAttachable {

    void attach();

    void detach();

    boolean isAttached();
}