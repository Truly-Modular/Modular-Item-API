package smartin.miapi.client.gui.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class State<T> {

    private T value;

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public State(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (Objects.equals(this.value, value))
            return;
        this.value = value;
        for (Consumer<T> listener : List.copyOf(listeners))
            listener.accept(value);
    }

    public void update(StateSubscriber stateSubscriber) {
        stateSubscriber.onStateChanged(this, value);
    }

    public Subscription subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);

        return () -> listeners.remove(listener);
    }

    public interface Subscription {
        void unsubscribe();
    }
}