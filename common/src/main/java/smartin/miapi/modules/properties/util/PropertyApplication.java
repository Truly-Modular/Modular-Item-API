package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.Event;
import smartin.miapi.mixin.LootContextTypesAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.PotionEffectProperty;

import java.util.*;
import java.util.function.BiPredicate;

public class PropertyApplication {
    public static void setup() {
        Event.LIVING_HURT.register(event -> {
            Cancellable<Event.LivingHurtEvent> ev = new Cancellable<>(event);
            ApplicationEvent.HURT.call(ev);
            return ev.result();
        });
        Event.LIVING_HURT_AFTER.register(event -> {
            ApplicationEvent.HURT_AFTER.call(event);
            return EventResult.pass();
        });
        PlayerEvent.PICKUP_ITEM_PRE.register((player, entity, stack) -> {
            Cancellable<ItemPickup> c = new Cancellable<>(new ItemPickup(player, entity, stack));
            ApplicationEvent.ITEM_PICKUP.call(c);
            return c.result();
        });
        PlayerEvent.PICKUP_ITEM_POST.register((player, entity, stack) -> ApplicationEvent.ITEM_PICKUP_AFTER.call(new ItemPickup(player, entity, stack)));
        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            ItemDrop drop = new ItemDrop(player, entity);
            ApplicationEvent.ITEM_DROP.call(drop);
            return drop.isCanceled ? EventResult.interruptFalse() : EventResult.pass();
        });
        EntityEvent.ENTER_SECTION.register((entity, x, y, z, prevX, prevY, prevZ) -> ApplicationEvent.ENTER_CHUNK.call(new EnterChunk(entity, x, y, z, prevX, prevY, prevZ)));
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

        private static final BiPredicate<Ability, ModuleProperty> abPred = (c, p) -> ItemModule.getMergedProperty(c.stack, p) != null;

        public static final ApplicationEvent<?> EMPTY = new ApplicationEvent<>("empty");
        public static final ApplicationEvent<Cancellable<Event.LivingHurtEvent>> HURT = new ApplicationEvent<>("hurt", "hit", "attack"); // fire when something gets hit
        public static final ApplicationEvent<Event.LivingHurtEvent> HURT_AFTER = new ApplicationEvent<>("hurt.after"); // fire after something gets hit and damage is confirmed
        public static final ApplicationEvent<Cancellable<ItemPickup>> ITEM_PICKUP = new ApplicationEvent<>((c, p) -> ItemModule.getMergedProperty(c.event.stack, p) != null, "item_pickup"); // fire when an item is picked up
        public static final ApplicationEvent<ItemPickup> ITEM_PICKUP_AFTER = new ApplicationEvent<>((c, p) -> ItemModule.getMergedProperty(c.stack, p) != null, "item_pickup.after"); // fire after an item is picked up
        public static final ApplicationEvent<ItemDrop> ITEM_DROP = new ApplicationEvent<>((c, p) -> ItemModule.getMergedProperty(c.entity.getStack(), p) != null, "item_drop"); // fire when an item is dropped
        public static final ApplicationEvent<EnterChunk> ENTER_CHUNK = new ApplicationEvent<>("enter_chunk"); // fire when an entity enters a new chunk
        public static final ApplicationEvent<Ability> ABILITY_START = new ApplicationEvent<>(abPred, "ability.start"); // fire when starting to use ability
        public static final ApplicationEvent<Ability> ABILITY_TICK = new ApplicationEvent<>(abPred, "ability.tick"); // fire every tick using an ability
        public static final ApplicationEvent<Ability> ABILITY_STOP = new ApplicationEvent<>(abPred, "ability.stop"); // fire when ABILITY_STOP_USING or ABILITY_STOP_HOLDING fires
        public static final ApplicationEvent<Ability> ABILITY_STOP_USING = new ApplicationEvent<>(abPred, "ability.stop.using"); // fire when releasing ability trigger
        public static final ApplicationEvent<Ability> ABILITY_STOP_HOLDING = new ApplicationEvent<>(abPred, "ability.stop.holding"); // fire when swapping items
        public static final ApplicationEvent<Ability> ABILITY_FINISH = new ApplicationEvent<>(abPred, "ability.finish"); // fire when ability timer runs out
        public static final ApplicationEvent<Ability> ABILITY_END = new ApplicationEvent<>(abPred, "ability.end"); // fire when any STOP or FINISH trigger fires

        public static final List<ApplicationEvent<Ability>> ABILITIES = List.of(ABILITY_START, ABILITY_TICK, ABILITY_STOP, ABILITY_STOP_USING, ABILITY_STOP_HOLDING, ABILITY_FINISH, ABILITY_END);

        private final List<ApplicationEventHandler> listeners = new ArrayList<>();
        public final String name;
        public final BiPredicate<E, ModuleProperty> predicate;
        public void addListener(ApplicationEventHandler handler) {
            listeners.add(handler);
        }

        public ApplicationEvent(String name, String... names) {
            this((e, p) -> true, name, names);
        }
        public ApplicationEvent(BiPredicate<E, ModuleProperty> predicate, String name, String... names) {
            this.predicate = predicate;
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

    public record Ability(ItemStack stack, World world, LivingEntity user, @Nullable Integer remainingUseTicks, ItemUseAbility ability) {
        public static LootContextType LOOT_CONTEXT = LootContextTypesAccessor.register("miapi:ability", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL));

        public int useTime() {
            if (remainingUseTicks == null) return ability.getMaxUseTime(stack);
            return ability.getMaxUseTime(stack) - remainingUseTicks;
        }
    }
    public static final class Cancellable<E> {
        private final E event;
        public boolean isCanceled = false;

        public Cancellable(E event) {
            this.event = event;
        }

        public E event() {
            return event;
        }

        public EventResult result() {
            return isCanceled ? EventResult.interruptFalse() : EventResult.pass();
        }
    }
    public record ItemPickup(PlayerEntity player, ItemEntity entity, ItemStack stack) {}
    public static final class ItemDrop {
        private final PlayerEntity player;
        private final ItemEntity entity;
        public boolean isCanceled = false;

        public ItemDrop(PlayerEntity player, ItemEntity entity) {
            this.player = player;
            this.entity = entity;
        }

        public PlayerEntity player() {
            return player;
        }

        public ItemEntity entity() {
            return entity;
        }

        @Override
        public String toString() {
            return "ItemDrop[" +
                    "player=" + player + ", " +
                    "entity=" + entity + ']';
        }
    }
    public record EnterChunk(Entity entity, int x, int y, int z, int prevX, int prevY, int prevZ) {}
}
