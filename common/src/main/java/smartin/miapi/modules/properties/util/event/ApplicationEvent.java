package smartin.miapi.modules.properties.util.event;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.PotionEffectProperty;
import smartin.miapi.modules.properties.util.ApplicationEventHandler;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.util.SimpleEventProperty;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * {@link ApplicationEvent} is a system used to manage {@link ModuleProperty}s that rely on various triggers in order to function.
 *
 * @param <E> Object passed into {@link ApplicationEventHandler}s when this event is called.
 * @see ApplicationEventHandler ApplicationEventHandler, a handler for the firing of events.
 * @see SimpleEventProperty SimpleEventProperty, an implementation of ApplicationEventHandler intended for module property use.
 * @see PotionEffectProperty PotionEffectProperty, an implementation of SimpleEventProperty.
 */
public class ApplicationEvent<E> {
    private static final Map<String, ApplicationEvent<?>> EVENT_NAMES = new HashMap<>();
    private static final List<ApplicationEvent<?>> EVENTS = new ArrayList<>();

    public static List<ApplicationEvent<?>> getAllEvents() {
        return EVENTS;
    }

    public static Codec<ApplicationEvent<?>> CODEC = Codec.STRING.xmap(
            ApplicationEvent::get,
            ev -> ev.name
    );

    public static ApplicationEvent<?> get(String name) {
        try {
            return EVENT_NAMES.get(name.toLowerCase().replaceAll("[.-]", "_"));
        } catch (Exception e) {
            Miapi.LOGGER.error("Failed to create ApplicationEvent from {}. Please using correct spelling: {}", name, EVENT_NAMES.keySet());
            e.printStackTrace();
        }
        return PropertyApplication.EMPTY;
    }

    @SafeVarargs
    public static <E> void trigger(E instance, ApplicationEvent<E>... events) {
        for (ApplicationEvent<E> event : events) {
            event.call(instance);
            //System.out.println("called " + event + " with " + instance);
        }
    }

    private final List<ApplicationEventHandler> listeners = new ArrayList<>();
    public final String name;
    public final BiPredicate<E, ModuleProperty> predicate;
    public final Function<E, ItemStack> stackGetter;

    public void addListener(ApplicationEventHandler handler) {
        listeners.add(handler);
    }

    public ApplicationEvent(String name, String... names) {
        this((e, p) -> true, e -> null, name, names);
    }

    public ApplicationEvent(BiPredicate<E, ModuleProperty> predicate, String name, String... names) {
        this(predicate, i -> null, name, names);
    }

    public ApplicationEvent(Function<E, ItemStack> stackGetter, String name, String... names) {
        this((e, p) -> ItemModule.getMergedProperty(stackGetter.apply(e), p) != null, stackGetter, name, names);
    }

    public ApplicationEvent(BiPredicate<E, ModuleProperty> predicate, Function<E, ItemStack> stackGetter, String name, String... names) {
        this.predicate = predicate;
        this.stackGetter = stackGetter;
        this.name = name;
        List<String> both = new ArrayList<>(Arrays.asList(names));
        both.add(0, name);
        for (String n : both) {
            EVENT_NAMES.put(n.toLowerCase().replaceAll("[.-]", "_"), this);
        }
        EVENTS.add(this);
    }

    public void call(E e) {
        this.listeners.forEach(handler -> {
            handler.onEvent(this, e);
        });
    }

    @Override
    public String toString() {
        return "ApplicationEvent{" +
                "name='" + name + '\'' +
                '}';
    }

    public static class EntityHolding<E> extends ApplicationEvent<E> {
        private final Function<E, LivingEntity> entityGetter;
        private final Function<E, Entity> altEntityGetter;

        public EntityHolding(Function<E, LivingEntity> entityGetter, Function<E, Entity> altEntityGetter, String name, String... names) {
            super(name, names);
            this.entityGetter = entityGetter;
            this.altEntityGetter = altEntityGetter;
        }

        public EntityHolding(BiPredicate<E, ModuleProperty> predicate, Function<E, LivingEntity> entityGetter, Function<E, Entity> altEntityGetter, String name, String... names) {
            super(predicate, name, names);
            this.entityGetter = entityGetter;
            this.altEntityGetter = altEntityGetter;
        }

        public EntityHolding(Function<E, ItemStack> stackGetter, Function<E, LivingEntity> entityGetter, Function<E, Entity> altEntityGetter, String name, String... names) {
            super(stackGetter, name, names);
            this.entityGetter = entityGetter;
            this.altEntityGetter = altEntityGetter;
        }

        public EntityHolding(BiPredicate<E, ModuleProperty> predicate, Function<E, ItemStack> stackGetter, Function<E, LivingEntity> entityGetter, Function<E, Entity> altEntityGetter, String name, String... names) {
            super(predicate, stackGetter, name, names);
            this.entityGetter = entityGetter;
            this.altEntityGetter = altEntityGetter;
        }

        public LivingEntity getEntity(E instance) {
            return entityGetter.apply(instance);
        }

        public @Nullable Entity getAlternateEntity(E instance) {
            return altEntityGetter.apply(instance);
        }

        public static boolean isTargetMain(String target) {
            switch (target) {
                case "alternative", "alternate", "victim", "2", "this" -> {
                    return false;
                }
                default -> {
                    return true;
                }
            }
        }
    }
}
