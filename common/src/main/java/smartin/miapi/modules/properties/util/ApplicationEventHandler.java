package smartin.miapi.modules.properties.util;

import smartin.miapi.modules.properties.PotionEffectProperty;
import smartin.miapi.modules.properties.util.event.ApplicationEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * {@link ApplicationEventHandler} is an interface used to manage the handling of {@link ApplicationEvent}s.
 * Implement the "onEvent" method to tell {@link ApplicationEvent}s how to handle each event respectively.
 * NOTE: You must register your event listening to each {@link ApplicationEvent} yourself.
 * For an example of how to do this, see {@link SimpleEventProperty}'s constructor.
 * @see ApplicationEvent
 * @see SimpleEventProperty
 * @see PotionEffectProperty
 */
public interface ApplicationEventHandler {
    class EventHandlingMap<V> extends HashMap<ApplicationEvent<V>, Consumer<V>> {
        public <E> void put(ApplicationEvent<E> event, Consumer<E> instance) {
            super.put((ApplicationEvent<V>) event, (Consumer<V>) instance); // trust me this is totally safe and not bad at all
        }

        public <E> EventHandlingMap<V> set(ApplicationEvent<E> event, Consumer<E> instance) {
            super.put((ApplicationEvent<V>) event, (Consumer<V>) instance);
            return this;
        }

        public <E> EventHandlingMap<V> set(ApplicationEvent<E> event, BiConsumer<ApplicationEvent<E>, E> instance) {
            super.put((ApplicationEvent<V>) event, v -> instance.accept(event, (E) v));
            return this;
        }

        public <E> EventHandlingMap<V> setAll(Collection<? extends ApplicationEvent<E>> events, Consumer<E> instance) {
            for (ApplicationEvent<E> event : events) {
                super.put((ApplicationEvent<V>) event, (Consumer<V>) instance);
            }
            return this;
        }

        public <E> EventHandlingMap<V> setAll(Collection<? extends ApplicationEvent<E>> events, BiConsumer<ApplicationEvent<E>, E> instance) {
            for (ApplicationEvent<E> event : events) {
                super.put((ApplicationEvent<V>) event, v -> instance.accept(event, (E) v));
            }
            return this;
        }

        public EventHandlingMap<V> setAll(BiConsumer<ApplicationEvent<V>, V> instance) {
            for (ApplicationEvent<?> event : ApplicationEvent.getAllEvents()) {
                super.put((ApplicationEvent<V>) event, v -> instance.accept((ApplicationEvent<V>) event, v));
            }
            return this;
        }

        public <E> Consumer<E> get(ApplicationEvent<E> event) {
            return (Consumer<E>) super.get(event);
        }
    }
    <E> void onEvent(ApplicationEvent<E> event, E instance);
}
