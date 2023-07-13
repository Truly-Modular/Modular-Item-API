package smartin.miapi.modules.properties.util;

import smartin.miapi.modules.properties.PotionEffectProperty;
import smartin.miapi.modules.properties.util.event.ApplicationEvent;

import java.util.function.Supplier;

/**
 * {@link SimpleEventProperty} is a class used to assist with {@link ApplicationEvent} handling.
 * @see ApplicationEvent
 * @see ApplicationEventHandler
 * @see PotionEffectProperty
 */
public abstract class SimpleEventProperty implements ApplicationEventHandler, ModuleProperty {
    private final EventHandlingMap<?> handlers;
    private final boolean requireModular;
    private final Supplier<ModuleProperty> property;

    public SimpleEventProperty(EventHandlingMap<?> map, boolean requireModular, Supplier<ModuleProperty> property) {
        this.handlers = map;
        this.requireModular = requireModular;
        this.property = property;
        for (ApplicationEvent<?> event : this.handlers.keySet()) {
            event.addListener(this);
        }
    }
    public SimpleEventProperty(EventHandlingMap<?> map) {
        this(map, false, () -> null);
    }

    @Override
    public <E> void onEvent(ApplicationEvent<E> event, E instance) {
        if (requireModular && !event.predicate.test(instance, property.get())) return;
        handlers.get(event).accept(instance);
    }
}
