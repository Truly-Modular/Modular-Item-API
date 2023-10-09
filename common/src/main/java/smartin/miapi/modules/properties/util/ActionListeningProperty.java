package smartin.miapi.modules.properties.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.Event;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public interface ActionListeningProperty {
    EventMap getAllListeners(ItemStack stack);

    class EventMap implements Iterable<Map.Entry<Event<?>, Object>> {
        protected final Multimap<Event<?>, Object> delegate;

        public EventMap() {
            this.delegate = HashMultimap.create();
        }

        public <T> void put(Event<T> event, T listener) {
            delegate.put(event, listener);
        }

        public <T> void putAll(EventMap other) {
            delegate.putAll(other.delegate);
        }

        public <T> EventMap add(Event<T> event, T listener) {
            delegate.put(event, listener);
            return this;
        }

        public <T> Collection<T> get(Event<T> event) {
            return (Collection<T>) delegate.get(event);
        }

        /**
         * unsafe, only use if you know what you are doing
         */
        public Multimap<Event<?>, Object> getUnwrappedDelegate() {
            return delegate;
        }

        @Override
        public Iterator<Map.Entry<Event<?>, Object>> iterator() {
            return delegate.entries().iterator();
        }

        public void forEach(BiConsumer<Event<?>, Object> consumer) {
            delegate.forEach(consumer);
        }
    }
}
