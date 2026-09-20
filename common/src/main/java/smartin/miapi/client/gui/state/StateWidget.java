package smartin.miapi.client.gui.state;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class StateWidget extends InteractAbleWidget implements StateSubscriber, UiAttachable {
    private boolean attached = true;
    private final List<State<?>> states = new ArrayList<>();
    private final List<State.Subscription> subscriptions = new ArrayList<>();

    protected StateWidget(int x, int y, int width, int height, Component title) {
        super(x, y, width, height, title);
    }


    /**
     * Register a State this widget depends on.
     * <p>
     * Multiple states can be registered.
     */
    public void subscribeTo(State<?> state) {
        if (states.contains(state))
            return;
        states.add(state);
        State.Subscription subscription =
                state.subscribe(value -> state.update(this));
        subscriptions.add(subscription);
    }


    /**
     * Called whenever one of the subscribed States changes.
     */
    public <T> void onStateChanged(State<T> state, T value) {
        if (!isAttached()) {
            for (State.Subscription subscription : subscriptions) {
                subscription.unsubscribe();
            }
        }
    }

    /**
     * Override these if StateWidget should follow your existing
     * InteractAbleWidget lifecycle.
     */
    @Override
    public void attach() {
        attached = true;
        for (GuiEventListener listener : children) {
            if (listener instanceof UiAttachable uiAttachable) {
                uiAttachable.attach();
            }
        }
    }

    public boolean isAttached() {
        return attached;
    }


    @Override
    public void removeChild(GuiEventListener listener) {
        super.removeChild(listener);
        if (listener instanceof UiAttachable uiAttachable) {
            uiAttachable.detach();
        }
    }

    @Override
    public void detach() {
        attached = false;
        for (GuiEventListener listener : children) {
            if (listener instanceof UiAttachable uiAttachable) {
                uiAttachable.detach();
            }
        }
    }
}