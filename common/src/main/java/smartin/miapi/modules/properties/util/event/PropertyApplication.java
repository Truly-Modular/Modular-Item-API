package smartin.miapi.modules.properties.util.event;

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
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.LootContextTypesAccessor;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

import java.util.List;

import static smartin.miapi.modules.properties.util.event.ApplicationEvent.EntityHolding;

public final class PropertyApplication {
    public static final ApplicationEvent<?> EMPTY = new ApplicationEvent<>("empty");
    public static final EntityHolding<ItemDrop> ITEM_DROP = new EntityHolding<>(c -> c.entity.getStack(), ItemDrop::player, ItemDrop::entity, "item_drop"); // fire when an item is dropped
    public static final EntityHolding<Cancellable<MiapiEvents.LivingHurtEvent>> HURT = new EntityHolding<>(c -> c.event.livingEntity, c -> c.event.damageSource.getAttacker(), "hurt", "hit", "attack"); // fire when something gets hit
    public static final EntityHolding<MiapiEvents.LivingHurtEvent> HURT_AFTER = new EntityHolding<>(c -> c.livingEntity, c -> c.damageSource.getAttacker(), "hurt.after"); // fire after something gets hit and damage is confirmed
    public static final EntityHolding<Cancellable<ItemPickup>> ITEM_PICKUP = new EntityHolding<>(c -> c.event.stack, c -> c.event.player, c -> c.event.entity, "item_pickup"); // fire when an item is picked up
    public static final EntityHolding<ItemPickup> ITEM_PICKUP_AFTER = new EntityHolding<>(c -> c.stack, ItemPickup::player, ItemPickup::entity, "item_pickup.after"); // fire after an item is picked up
    public static final ApplicationEvent<EnterChunk> ENTER_CHUNK = new ApplicationEvent<>("enter_chunk"); // fire when an entity enters a new chunk
    public static final EntityHolding<Ability> ABILITY_START = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.start"); // fire when starting to use ability
    public static final EntityHolding<Ability> ABILITY_TICK = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.tick"); // fire every tick using an ability
    public static final EntityHolding<Ability> ABILITY_STOP = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.stop"); // fire when ABILITY_STOP_USING or ABILITY_STOP_HOLDING fires
    public static final EntityHolding<Ability> ABILITY_STOP_USING = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.stop.using"); // fire when releasing ability trigger
    public static final EntityHolding<Ability> ABILITY_STOP_HOLDING = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.stop.holding"); // fire when swapping items
    public static final EntityHolding<Ability> ABILITY_FINISH = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.finish"); // fire when ability timer runs out
    public static final EntityHolding<Ability> ABILITY_END = new EntityHolding<>(Ability::stack, Ability::user, a -> null, "ability.end"); // fire when any STOP or FINISH trigger fires

    public static final List<EntityHolding<Ability>> ABILITIES = List.of(ABILITY_START, ABILITY_TICK, ABILITY_STOP, ABILITY_STOP_USING, ABILITY_STOP_HOLDING, ABILITY_FINISH, ABILITY_END);


    public static void setup() {
        MiapiEvents.LIVING_HURT.register(event -> {
            Cancellable<MiapiEvents.LivingHurtEvent> ev = new Cancellable<>(event);
            HURT.call(ev);
            return ev.result();
        });
        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            HURT_AFTER.call(event);
            return EventResult.pass();
        });
        PlayerEvent.PICKUP_ITEM_PRE.register((player, entity, stack) -> {
            Cancellable<ItemPickup> c = new Cancellable<>(new ItemPickup(player, entity, stack));
            ITEM_PICKUP.call(c);
            return c.result();
        });
        PlayerEvent.PICKUP_ITEM_POST.register((player, entity, stack) -> ITEM_PICKUP_AFTER.call(new ItemPickup(player, entity, stack)));
        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            ItemDrop drop = new ItemDrop(player, entity);
            ITEM_DROP.call(drop);
            return drop.isCanceled ? EventResult.interruptFalse() : EventResult.pass();
        });
        EntityEvent.ENTER_SECTION.register((entity, x, y, z, prevX, prevY, prevZ) -> ENTER_CHUNK.call(new EnterChunk(entity, x, y, z, prevX, prevY, prevZ)));
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
