package smartin.miapi.events.property;

import com.mojang.datafixers.util.Pair;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import dev.architectury.event.Event;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A class holding all of Miapi's base {@link ApplicationEvent}s.
 * Additionally, it also contains a few helpers for {@link ApplicationEvent}s or their listeners.
 * {@link #entityEvents}: A list containing every entity related event. This is used so that listeners
 * can easily listen to and handle every entity related event at the same time.
 * {@link #entityDataReaders}: A registry representing ways to get items out of entities.
 * By default, you can check entities' offhands, mainhands, armor slots, or active using item. This is
 * also used to get the itemstack in modular projectile entities, or the itemstack in dropped ItemEntities.
 * {@link #entityRedirectors}: A registry representing ways to get other entities from
 * an inputted one. By default, you can the entity's attacker, or the entity's victim.
 * <p></p>
 * Example usages of these helpers can be seen in the PotionEffectProperty.
 */
public class ApplicationEvents {
    public static final Map<String, Function<Entity, ItemStack>> entityDataReaders = new HashMap<>();
    private static final Map<String, Function<Entity, Entity>> entityRedirectors = new HashMap<>();
    private static final List<BiConsumer<EntityInvoker, StackGetterHolder<?>>> entityEvents = new ArrayList<>();
    public static void registerEntityRedirector(String key, Function<Entity, Entity> redirector) {
        entityRedirectors.put(key, redirector);
    }
    public static Function<Entity, Entity> getEntityRedirector(String key) {
        return entityRedirectors.getOrDefault(key, e -> null);
    }

    /**
     * Gets the desired target for some property's application. (See this class's javadoc section for the {@link #entityRedirectors})
     * @see smartin.miapi.modules.properties.PotionEffectProperty PotionEffectProperty, for usage examples
     * @param target the string representation of the target. Ex. 'victim', 'victim.attacker', 'this', 'attacker.attacking'
     * @param entities a map representing all potential prefixes for the 'target' parameter.
     *                 In this examples above, this would include the 'victim', 'this', and 'attacker'.
     * @param fallback the fallback entity if the map does not contain the target prefix
     * @return the entity determined by the target string.
     */
    public static @Nullable Entity getEntityForTarget(String target, Map<String, Entity> entities, Entity fallback) {
        String[] segments = target.split("\\.");
        Entity entity = entities.getOrDefault(segments[0], fallback);
        if (entity == null) return null;
        return segments.length == 1 ? entity : getEntityRedirector(segments[1]).apply(entity);
    }

    /**
     * Registers a new entity based event. See this class's javadoc for more info.
     * @param consumer A biconsumer holding the EntityInvoker (use to actually call this event)
     *                 and the {@link StackGetterHolder}.
     */
    public static void registerEntityEvent(BiConsumer<EntityInvoker, StackGetterHolder<?>> consumer) {
        entityEvents.add(consumer);
    }

    /**
     * Registers a new entity data reader. See this class's javadoc for more info.
     */
    public static void registerEntityDataReader(String key, Function<Entity, ItemStack> stackGetter) {
        entityDataReaders.put(key, stackGetter);
    }

    public static void setup() {
        /*// Nucleus's autocodec override for ApplicationEvents - now baked into the ApplicationEvent via annotation
        AutoCodec.addInherit(ApplicationEvent.class, () -> ApplicationEvent.codec);*/

        // ENTITY EVENTS
        registerEntityEvent((listener, stackGetter) -> {
            HURT.startListening((stack, selected, data, victim, source, amount) -> {
                listener.call(HURT, selected, stack, data, victim, source, amount);
            }, stackGetter);
        });
        registerEntityEvent((listener, stackGetter) -> {
            HURT_AFTER.startListening((stack, selected, data, victim, source, amount) -> {
                listener.call(HURT_AFTER, selected, stack, data, victim, source, amount);
            }, stackGetter);
        });

        registerEntityEvent((listener, stackGetter) -> {
            ENTER_CHUNK.startListening((stack, data, entity, x, y, z, px, py, pz) -> {
                listener.call(ENTER_CHUNK, entity, stack, data, x, y, z, px, py, pz);
            }, stackGetter);
        });

        PlayerEvent.DROP_ITEM.register(((player, entity) -> {
            ITEM_DROP.invoker().call(entity.getStack(), entity, player);
            return EventResult.pass();
        }));
        registerEntityEvent((listener, stackGetter) -> {
            ITEM_DROP.startListening((targetStack, targetEntity, data, stack, entity, player) -> {
                listener.call(ITEM_DROP, targetEntity, targetStack, data, stack, entity, player);
            }, stackGetter);
        });

        PlayerEvent.PICKUP_ITEM_POST.register((player, entity, stack) -> ITEM_PICKUP.invoker().call(stack, entity, player));
        registerEntityEvent((listener, stackGetter) -> {
            ITEM_PICKUP.startListening((targetStack, targetEntity, data, stack, entity, player) -> {
                listener.call(ITEM_PICKUP, targetEntity, targetStack, data, stack, entity, player);
            }, stackGetter);
        });

        registerEntityEvent((listener, stackGetter) -> {
            PLAYER_TICK.startListening((stack, data, player) -> {
                listener.call(PLAYER_TICK, player, stack, data, stack);
            }, stackGetter);
        });

        ABILITY_EVENTS.forEach(event -> {
            registerEntityEvent((listener, stackGetter) -> {
                event.startListening((stack, data, usingStack, world, user, remaining, ability) -> {
                    listener.call(event, user, stack, data, usingStack, world, remaining, ability);
                }, stackGetter);
            });
        });


        // DATA READERS
        registerEntityDataReader("mainhand", entity -> entity instanceof LivingEntity living ? living.getMainHandStack() : null);
        registerEntityDataReader("offhand", entity -> entity instanceof LivingEntity living ? living.getOffHandStack() : null);
        registerEntityDataReader("head", entity -> entity instanceof LivingEntity living ? living.getEquippedStack(EquipmentSlot.HEAD) : null);
        registerEntityDataReader("chest", entity -> entity instanceof LivingEntity living ? living.getEquippedStack(EquipmentSlot.CHEST) : null);
        registerEntityDataReader("legs", entity -> entity instanceof LivingEntity living ? living.getEquippedStack(EquipmentSlot.LEGS) : null);
        registerEntityDataReader("feet", entity -> entity instanceof LivingEntity living ? living.getEquippedStack(EquipmentSlot.FEET) : null);
        registerEntityDataReader("projectile", entity -> {
            if (entity instanceof ProjectileEntity projectile && projectile instanceof ItemProjectile itemProjectile)
                return itemProjectile.asItemStack();
            return null;
        });
        registerEntityDataReader("using", entity -> entity instanceof LivingEntity living ? living.getActiveItem() : null);
        registerEntityDataReader("dropped", entity -> entity instanceof ItemEntity item ? item.getStack() : null);

        // ENTITY REDIRECTORS
        registerEntityRedirector("attacker", entity -> entity instanceof LivingEntity living ? living.getAttacker() : null);
        registerEntityRedirector("attacking", entity -> entity instanceof LivingEntity living ? living.getAttacking() : null);
    }

    public static ApplicationEvent.Dynamic<EntityInvoker, StackGetterHolder<?>> ENTITY_RELATED = new ApplicationEvent.Dynamic<>("entity_event") {
        @Override
        protected boolean canCall(Object[] params, StackGetterHolder<?> additionalData) {
            return true;
        }

        @Override
        public void startListening(EntityInvoker listener, StackGetterHolder<?> additionalData) {
            super.startListening(listener, additionalData);
            entityEvents.forEach(consumer -> {
                consumer.accept(listener, additionalData);
            });
        }
    };
    public static HurtEvent HURT = new HurtEvent("hurt", MiapiEvents.LIVING_HURT); // when an entity takes damage
    public static HurtEvent HURT_AFTER = new HurtEvent("hurt.after", MiapiEvents.LIVING_HURT_AFTER); // after an entity takes damage (damage is confirmed to apply)
    public static PlayerTickEvent PLAYER_TICK = new PlayerTickEvent("player_tick"); // after a player ticks
    public static EnterChunkEvent ENTER_CHUNK = new EnterChunkEvent("enter_chunk"); // when an entity enters a new chunk
    public static ItemEntityEvent ITEM_DROP = new ItemEntityEvent("item_drop"); // when a player drops an item
    public static ItemEntityEvent ITEM_PICKUP = new ItemEntityEvent("item_pickup"); // when a player picks up an item
    public static AbilityEvent ABILITY_START = new AbilityEvent("ability.start"); // when a player starts an ItemUseAbility
    public static AbilityEvent ABILITY_TICK = new AbilityEvent("ability.tick"); // when a player's active ItemUseAbility ticks
    public static AbilityEvent ABILITY_STOP = new AbilityEvent("ability.stop"); // when a player stops an ItemUseAbility
    public static AbilityEvent ABILITY_STOP_USING = new AbilityEvent("ability.stop.using"); // when a player stops an ItemUseAbility- specifically stops using(stops holding right click)
    public static AbilityEvent ABILITY_STOP_HOLDING = new AbilityEvent("ability.stop.holding"); // when a player stops an ItemUseAbility- specifically stops holding the use item
    public static AbilityEvent ABILITY_FINISH = new AbilityEvent("ability.finish"); // when a player's ItemUseAbility timer runs out
    public static List<AbilityEvent> ABILITY_EVENTS;

    public interface EntityInvoker {
        /**
         * @param event the actual event that called the EntityEvent
         * @param entity the entity
         * @param stack the selected stack
         * @param data data passed in by the {@link StackGetterHolder} when adding the listener
         * @param originalParams the original parameters of the actual event calling this EntityEvent
         */
        void call(ApplicationEvent<?, ?, ?> event, Entity entity, ItemStack stack, Object data, Object... originalParams);
    }

    public static class HurtEvent extends ApplicationEvent<HurtInvoker, HurtListener, StackGetterHolder<?>> {
        public HurtEvent(String name, Event<MiapiEvents.LivingHurt> hurtEvent) {
            super(name);
            hurtEvent.register(event -> {
                this.invoker().call(event.livingEntity, event.damageSource, event.amount);
                return EventResult.pass();
            });
        }

        @Override
        protected void callWithInvokerParams(HurtListener toCall, Object[] params, StackGetterHolder<?> additionalData) {
            LivingEntity entity = (LivingEntity) params[0];
            DamageSource source = (DamageSource) params[1];
            float amount = (float) params[2];

            Entity attacker = source.getAttacker();
            Entity direct = source.getSource();

            // supports access to the victim, attacker, and direct source
            additionalData.checkAndCall(
                    (stack, targeted, data) -> toCall.call(stack, targeted, data, entity, source, amount),
                    ".",
                    Pair.of("victim", entity),
                    Pair.of("attacker", attacker),
                    Pair.of("source", direct)
            );
        }

        @Override
        public void startListening(HurtListener listener) {
            throw new IllegalArgumentException("Cannot listen to a dynamic ApplicationEvent (HurtEvent) without providing additional data!");
        }
    }
    public interface HurtInvoker {
        void call(LivingEntity entity, DamageSource source, float amount);
    }
    public interface HurtListener {
        /**
         * @param stack    the targeted stack chosen by the passed in {@link StackGetterHolder}
         * @param selected ^ but entity
         * @param data     ^ but data used for the {@link StackGetterHolder}
         * @param victim   the original victim, or entity taking damage
         * @param source   the damage source
         * @param amount   the damage amount
         */
        void call(ItemStack stack, Entity selected, Object data, LivingEntity victim, DamageSource source, float amount);
    }

    public static class AbilityEvent extends ApplicationEvent<AbilityInvoker, AbilityListener, StackGetterHolder<?>> {
        public AbilityEvent(String name) {
            super(name);
            if (ABILITY_EVENTS == null) ABILITY_EVENTS = new ArrayList<>();
            ABILITY_EVENTS.add(this);
        }

        @Override
        protected void callWithInvokerParams(AbilityListener toCall, Object[] params, StackGetterHolder<?> additionalData) {
            ItemStack using = (ItemStack) params[0];
            World world = (World) params[1];
            LivingEntity user = (LivingEntity) params[2];
            Integer remainingUsingTicks = params[3] == null ? null : (Integer) params[3];
            ItemUseAbility ability = (ItemUseAbility) params[4];

            additionalData.checkAndCall(
                    (stack, entity, o) -> toCall.call(stack, o, using, world, user, remainingUsingTicks, ability),
                    user
            );
        }

        @Override
        public void startListening(AbilityListener listener) {
            throw new IllegalArgumentException("Cannot listen to a dynamic ApplicationEvent (AbilityEvent) without providing additional data!");
        }
    }
    public interface AbilityInvoker {
        void call(ItemStack usingStack, World world, LivingEntity user, @Nullable Integer remainingUseTicks, ItemUseAbility ability);
    }
    public interface AbilityListener {
        /**
         * @param stack             the targeted stack chosen by the passed in {@link StackGetterHolder}
         * @param data              ^ but data used for the {@link StackGetterHolder}
         * @param usingStack        the itemstack being used by the user
         * @param world             the user's world
         * @param user              the user of the item
         * @param remainingUseTicks the amount of time left before the usability expires
         * @param ability           the currently active ItemUseAbility
         */
        void call(ItemStack stack, Object data, ItemStack usingStack, World world, LivingEntity user, @Nullable Integer remainingUseTicks, ItemUseAbility ability);
    }

    public static class EnterChunkEvent extends ApplicationEvent<EnterChunkInvoker, EnterChunkListener, StackGetterHolder<?>> {
        public EnterChunkEvent(String name) {
            super(name);
            EntityEvent.ENTER_SECTION.register((entity, x, y, z, px, py, pz) ->
                    this.invoker().call(entity, x, y, z, px, py, pz));
        }

        @Override
        protected void callWithInvokerParams(EnterChunkListener toCall, Object[] params, StackGetterHolder<?> additionalData) {
            Entity entity = (Entity) params[0];
            int x = (int) params[1];
            int y = (int) params[2];
            int z = (int) params[3];
            int px = (int) params[4]; // p for previous
            int py = (int) params[5];
            int pz = (int) params[6];

            additionalData.checkAndCall(
                    (stack, target, data) -> toCall.call(stack, data, entity, x, y, z, px, py, pz),
                    entity
            );
        }

        @Override
        public void startListening(EnterChunkListener listener) {
            throw new IllegalArgumentException("Cannot listen to a dynamic ApplicationEvent (EnterChunkEvent) without providing additional data!");
        }
    }
    public interface EnterChunkInvoker {
        void call(Entity entity, int x, int y, int z, int prevX, int prevY, int prevZ);
    }
    public interface EnterChunkListener {
        void call(ItemStack stack, Object data, Entity entity, int x, int y, int z, int prevX, int prevY, int prevZ);
    }

    public static class ItemEntityEvent extends ApplicationEvent<ItemEntityInvoker, ItemEntityListener, StackGetterHolder<?>> {
        public ItemEntityEvent(String name) {
            super(name);
        }

        @Override
        protected void callWithInvokerParams(ItemEntityListener toCall, Object[] params, StackGetterHolder<?> additionalData) {
            ItemStack stack = (ItemStack) params[0];
            ItemEntity entity = (ItemEntity) params[1];
            PlayerEntity player = (PlayerEntity) params[2];

            additionalData.checkAndCall(
                    (targetedStack, target, data) -> toCall.call(targetedStack, target, data, stack, entity, player),
                    ".",
                    Pair.of("entity", entity),
                    Pair.of("player", player)
            );
        }

        @Override
        public void startListening(ItemEntityListener listener) {
            throw new IllegalArgumentException("Cannot listen to a dynamic ApplicationEvent (ItemEntityEvent) without providing additional data!");
        }
    }
    public interface ItemEntityInvoker {
        void call(ItemStack stack, ItemEntity itemEntity, PlayerEntity player);
    }
    public interface ItemEntityListener {
        void call(ItemStack targetStack, Entity target, Object object, ItemStack droppedStack, ItemEntity itemEntity, PlayerEntity player);
    }

    public static class PlayerTickEvent extends ApplicationEvent<PlayerTickInvoker, PlayerTickListener, StackGetterHolder<?>> {
        public PlayerTickEvent(String name) {
            super(name);
            TickEvent.PLAYER_POST.register(p -> this.invoker().call(p));
        }

        @Override
        protected void callWithInvokerParams(PlayerTickListener toCall, Object[] params, StackGetterHolder<?> additionalData) {
            PlayerEntity player = (PlayerEntity) params[0];

            additionalData.checkAndCall(
                    (stack, entity, data) -> toCall.call(stack, data, player),
                    player
            );
        }
    }
    public interface PlayerTickInvoker {
        void call(PlayerEntity player);
    }
    public interface PlayerTickListener {
        void call(ItemStack stack, Object data, PlayerEntity entity);
    }

    /**
     * StackGetterHolder is a helper class used to dynamically control which ItemStack should be used
     * for some ApplicationEvent.
     * If you're interested in creating a StackGetterHolder for some ApplicationEvent's listener,here's what you should know:
     * - If your property's data is a list, or possibly map, use the {@link #ofMulti(Function, Function, BiFunction)} method for creation.
     * - If your property's data represents a single object, use the {@link #ofSingle(Function, Function)} method for creation.
     * - The first parameter is a function providing an item stack, requiring you to return some data. Usually this is your property's data, based on the ItemStack.
     * - The second parameter provides you the data you just returned, and requires you to filter out for some string value.
     *   This string value represents the target item for the event. Eg. 'victim.mainhand', 'attacker.head'
     *   (If you're creating with ofMulti, you will also have to define a third parameter to filter for values that match this target item.)
     * <p></p>
     * If you're interested in using the StackGetterHolder for a custom ApplicationEvent, here's what you should know:
     * - Set the StackGetterHolder to your event's additional data parameter, or at least something that includes it.
     * - In your {@link ApplicationEvent#callWithInvokerParams(Object, Object[], Object)}, call the {@link #checkAndCall(TriConsumer, String, Pair[])}
     *   method to check the target ItemStack against the entities you input.
     *
     * @see smartin.miapi.modules.properties.PotionEffectProperty PotionEffectProperty, for example usage
     * @param <T>
     */
    public static class StackGetterHolder<T> {
        protected final Function<ItemStack, T> dataGetter;
        protected final @Nullable Function<T, String> single;
        protected final @Nullable Function<T, List<String>> multi;
        protected final @Nullable BiFunction<T, String, T> filter;

        public StackGetterHolder(Function<ItemStack, T> dataGetter, @Nullable Function<T, String> single, @Nullable Function<T, List<String>> multi, @Nullable BiFunction<T, String, T> filter) {
            this.dataGetter = dataGetter;
            this.single = single;
            this.multi = multi;
            this.filter = filter;
        }
        public static <T> StackGetterHolder<T> ofSingle(Function<ItemStack, T> dataGetter, Function<T, String> single) {
            return new StackGetterHolder<>(dataGetter, single, null, (t, str) -> t);
        }
        public static <T> StackGetterHolder<T> ofMulti(Function<ItemStack, T> dataGetter, Function<T, List<String>> multi, BiFunction<T, String, T> filter) {
            return new StackGetterHolder<>(dataGetter, null, multi, filter);
        }

        public void ifSingle(BiConsumer<Function<ItemStack, T>, Function<T, String>> consumer) {
            if (single != null)
                consumer.accept(dataGetter, single);
        }
        public void ifMulti(BiConsumer<Function<ItemStack, T>, Function<T, List<String>>> consumer) {
            if (multi != null)
                consumer.accept(dataGetter, multi);
        }

        /**
         * {@link #checkAndCall(TriConsumer, String, Pair[])}, but only with one entity as an input. The resulting
         * format will be: 'mainhand' or 'head', both sampling the input entity.
         */
        public void checkAndCall(TriConsumer<ItemStack, Entity, T> consumer, Entity entity) {
            checkAndCall(consumer, "", Pair.of("", entity));
        }

        /**
         * A method used to check target items against inputted entities.
         * @param consumer  the code to run for everything the target item matches. This is usually calling your ApplicationEvent's listeners.
         * @param separator the string to separate the entity and entityDataReader. Eg. 'victim.mainhand' -> "."
         * @param entities  the key to entity pairs for checking target items. 'victim', 'attacker', etc.
         */
        @SafeVarargs
        public final void checkAndCall(TriConsumer<ItemStack, Entity, T> consumer, String separator, Pair<String, Entity>... entities) {
            this.ifSingle((dataGetter, targetGetter) -> {
                entityDataReaders.forEach((key, stackGetter) -> { // looping all entity data readers, so you can add new things like curio inventory checks
                    for (Pair<String, Entity> pair : entities) {
                        ItemStack stack;
                        T data;
                        String result;
                        String prefix = pair.getFirst();
                        Entity entity = pair.getSecond();
                        if (entity != null &&
                                (stack = stackGetter.apply(entity)) != null && // making sure the stack can be gotten from the entity (most entityDataReaders have additional checks, and return null if they fail)
                                !stack.isEmpty() && // no empties allowed 😠
                                (data = dataGetter.apply(stack)) != null && // making sure the data is actually obtainable from the stack (properties often return null if the item does not have the property)
                                (result = targetGetter.apply(data)) != null && // making sure the target string can actually be obtained from the data
                                result.equals(prefix + separator + key)) { // if matches
                            consumer.accept(stack, entity, data); // call consumer with data, target stack, and entity
                            break; // I break because logically, if this works for entity x, which uses prefix x, how would it work with entity y, who has prefix y?
                        }
                    }
                });
            });
            this.ifMulti((dataGetter, targetGetter) -> {
                entityDataReaders.forEach((key, stackGetter) -> {
                    assert filter != null; // can't not have a filter when using multi
                    for (Pair<String, Entity> pair : entities) {
                        ItemStack stack;
                        T data;
                        List<String> result;
                        String prefix = pair.getFirst();
                        Entity entity = pair.getSecond();
                        if (entity != null && (stack = stackGetter.apply(entity)) != null && !stack.isEmpty()) // look above for explanation, it's practically the same thing
                            if ((data = dataGetter.apply(stack)) != null && (result = targetGetter.apply(data)) != null) result.forEach(expected -> { // looping the list of potential targets
                                if (expected.equals(prefix + separator + key)) {
                                    consumer.accept(stack, entity, filter.apply(data, expected)); // call consumer and filter data
                                }
                            });
                    }
                });
            });
        }
    }
}
