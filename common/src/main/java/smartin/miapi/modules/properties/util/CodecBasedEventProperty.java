package smartin.miapi.modules.properties.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Combination of {@link SimpleEventProperty} and {@link CodecBasedProperty}.
 * (Also has a couple additional features)
 */
public abstract class CodecBasedEventProperty<T> extends CodecBasedProperty<T> implements ApplicationEventHandler {
    private final EventHandlingMap<?> handlers;
    private final boolean requireModular;
    private final @Nullable Function<T, PropertyApplication.ApplicationEvent<?>> eventGetter;

    public CodecBasedEventProperty(String key, boolean requireModular, EventHandlingMap<?> map, Function<T, PropertyApplication.ApplicationEvent<?>> eventGetter) {
        super(key);
        this.handlers = map;
        this.requireModular = requireModular;
        this.eventGetter = eventGetter;
        for (PropertyApplication.ApplicationEvent<?> event : this.handlers.keySet()) {
            event.addListener(this);
        }
    }
    public CodecBasedEventProperty(String key, EventHandlingMap<?> map) {
        this(key, false, map, null);
    }
    public CodecBasedEventProperty(String key, EventHandlingMap<?> map, Function<T, PropertyApplication.ApplicationEvent<?>> eventGetter) {
        this(key, false, map, eventGetter);
    }

    @Override
    public <E> void onEvent(PropertyApplication.ApplicationEvent<E> event, E instance) {
        if (requireModular && !event.predicate.test(instance, this)) return;
        if (eventGetter != null && !eventGetter.apply(this.get(event.stackGetter.apply(instance))).equals(event)) return;
        handlers.get(event).accept(instance);
    }
}
