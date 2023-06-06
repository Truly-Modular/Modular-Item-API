package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.Event;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.PotionEffectProperty;

import java.util.*;

public class PropertyApplication {
    public static void init() {
        Event.LIVING_HURT.register(event -> {
            ApplicationEvent.HURT.call(event);

            return EventResult.pass();
        });
    }

    /**
     * {@link ApplicationEvent} is a system used to manage {@link ModuleProperty}s that rely on various triggers in order to function.
     *
     * @param <E> Object passed into {@link ApplicationEventHandler}s when this event is called.
     * @see ApplicationEventHandler ApplicationEventHandler, a handler for the firing of events.
     * @see SimpleEventProperty SimpleEventProperty, an implementation of ApplicationEventHandler intended for module property use.
     * @see PotionEffectProperty PotionEffectProperty, an implementation of SimpleEventProperty.
     */
    public static class ApplicationEvent<E> {
        private static final Map<String, ApplicationEvent<?>> EVENT_NAMES = new HashMap<>();
        private static final List<ApplicationEvent<?>> EVENTS = new ArrayList<>();
        public static List<ApplicationEvent<?>> getAllEvents() {
            return EVENTS;
        }
        public static ApplicationEvent<?> get(String name) {
            try {
                return EVENT_NAMES.get(name.toLowerCase().replaceAll("[.-]", "_"));
            } catch (Exception e) {
                Miapi.LOGGER.error("Failed to create ApplicationEvent from {}. Please using correct spelling: {}", name, EVENT_NAMES.keySet());
                e.printStackTrace();
            }
            return EMPTY;
        }
        @SafeVarargs
        public static <E> void trigger(E instance, ApplicationEvent<E>... events) {
            for (ApplicationEvent<E> event : events) {
                event.call(instance);
            }
        }

        public static final ApplicationEvent<?> EMPTY = new ApplicationEvent<>("empty");
        public static final ApplicationEvent<Event.LivingHurtEvent> HURT = new ApplicationEvent<>("hurt", "hit", "attack"); // fire when something gets hit
        public static final ApplicationEvent<Holders.Ability> ABILITY_START = new ApplicationEvent<>("ability.start"); // fire when starting to use ability
        public static final ApplicationEvent<Holders.Ability> ABILITY_TICK = new ApplicationEvent<>("ability.tick"); // fire every tick using an ability
        public static final ApplicationEvent<Holders.Ability> ABILITY_STOP = new ApplicationEvent<>("ability.stop"); // fire when ABILITY_STOP_USING or ABILITY_STOP_HOLDING fires
        public static final ApplicationEvent<Holders.Ability> ABILITY_STOP_USING = new ApplicationEvent<>("ability.stop.using"); // fire when releasing ability trigger
        public static final ApplicationEvent<Holders.Ability> ABILITY_STOP_HOLDING = new ApplicationEvent<>("ability.stop.holding"); // fire when swapping items
        public static final ApplicationEvent<Holders.Ability> ABILITY_FINISH = new ApplicationEvent<>("ability.finish"); // fire when ability timer runs out
        public static final ApplicationEvent<Holders.Ability> ABILITY_END = new ApplicationEvent<>("ability.end"); // fire when any STOP or FINISH trigger fires

        public static final List<ApplicationEvent<Holders.Ability>> ABILITIES = List.of(ABILITY_START, ABILITY_TICK, ABILITY_STOP, ABILITY_STOP_USING, ABILITY_STOP_HOLDING, ABILITY_FINISH, ABILITY_END);

        private final List<ApplicationEventHandler> listeners = new ArrayList<>();
        public final String name;
        public void addListener(ApplicationEventHandler handler) {
            listeners.add(handler);
        }

        public ApplicationEvent(String name, String... names) {
            this.name = name;
            List<String> both = new ArrayList<>(Arrays.asList(names));
            both.add(0, name);
            for (String n : both) {
                EVENT_NAMES.put(n.toLowerCase().replaceAll("[.-]", "_"), this);
            }
            EVENTS.add(this);
        }

        public void call(E e) {
            this.listeners.forEach(handler -> handler.onEvent(this, e));
        }

        @Override
        public String toString() {
            return "ApplicationEvent{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static final class Holders {
        public record Ability(ItemStack stack, World world, LivingEntity user, @Nullable Integer remainingUseTicks, ItemUseAbility ability) {}
    }
}
