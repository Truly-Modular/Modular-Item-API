package smartin.miapi.client.gui.state;

/**
 * interface for new state system
 */
public interface StateSubscriber {

    void onStateChanged(State<?> state);

    void subscribeTo(State<?> state);

    /**
     * Called whenever one of the subscribed States changes.
     */
    <T> void onStateChanged(State<T> state, T value);

    void unsubscribeFromStates();
}