package smartin.miapi.modules.abilities.util;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.*;

import static smartin.miapi.modules.properties.util.PropertyApplication.ApplicationEvent.*;
import static smartin.miapi.modules.properties.util.PropertyApplication.Ability;

/**
 * The ItemAbilityManager is the brain and control behind what Ability is executed on what Item.
 * Abilities need to be added in the Module Json so the {@link AbilityProperty} can pick them up properly
 * This class then checks all provided abilites and delegates the calls from the {@link net.minecraft.item.Item} to the {@link ItemUseAbility}
 */
public class ItemAbilityManager {
    private static final Map<PlayerEntity, ItemStack> playerActiveItems = new HashMap<>();
    private static final Map<PlayerEntity, ItemStack> playerActiveItemsClient = new HashMap<>();
    public static final MiapiRegistry<ItemUseAbility> useAbilityRegistry = MiapiRegistry.getInstance(ItemUseAbility.class);
    private static final EmptyAbility emptyAbility = new EmptyAbility();
    private static final Map<String, ItemUseAbility> abilityMap = new WeakHashMap<>(); // cache uuid -> ability map... blame smartin for uuid as a string
    private static final Map<String, ItemUseAbility> abilityMapClient = new WeakHashMap<>(); // ^ but client

    public static void setup() {
        TickEvent.PLAYER_PRE.register((playerEntity) -> {
            Map<PlayerEntity, ItemStack> activeItems = playerActiveItems;
            Map<String, ItemUseAbility> activeAbilities = abilityMap;
            if (playerEntity.getWorld().isClient) {
                activeItems = playerActiveItemsClient;
                activeAbilities = abilityMapClient;
            }

            ItemStack oldItem = activeItems.get(playerEntity);
            ItemStack playerItem = playerEntity.getActiveItem();

            int oldSlot = oldItem == null || oldItem.isEmpty() ? -1 : playerEntity.getInventory().getSlotWithStack(oldItem);
            int slot = playerEntity.getInventory().selectedSlot;

            /*
            Condition for making sure the slot was just switched. Requires that:
            - The player's current slot is different to the slot of the old using item.
              This works because if the item is not in your inventory, the slot value will be -1.
            - Either the old using item is null, or is empty.
              It will be null when this is the player's first ever item use, and it will be empty after they finish using it. (see the activeItems.put(...))
            - The player's current using item is empty. This ensures that the player is not still using the item.
             */
            if (playerItem != null && oldSlot != slot && (oldItem == null || !oldItem.isEmpty()) && playerItem.isEmpty()) {
                activeItems.put(playerEntity, playerItem.copy());
                if (oldItem != null) {
                    ItemUseAbility ability = getAbility(oldItem);

                    trigger(new Ability(oldItem, playerEntity.getWorld(), playerEntity, playerEntity.getItemUseTimeLeft(), ability), ABILITY_END, ABILITY_STOP, ABILITY_STOP_HOLDING);
                    ability.onStoppedHolding(oldItem, playerEntity.getWorld(), playerEntity);

                    removeFromAbilityMap(oldItem, activeAbilities);
                }
            } else if (playerItem != null && oldItem != null && oldItem.isEmpty() && !playerItem.isEmpty())
                activeItems.put(playerEntity, playerItem.copy());
        });
        useAbilityRegistry.register("empty", emptyAbility);
    }

    public static @Nullable String getCacheUUID(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ModularItem && stack.hasNbt() && stack.getNbt().contains("miapiuuid", 8)) // type 8 = string
            return stack.getNbt().getString("miapiuuid");
        return null;
    }
    public static void removeFromAbilityMap(ItemStack stack, Map<String, ItemUseAbility> map) {
        String uuid = getCacheUUID(stack);
        if (uuid != null) map.remove(uuid);
    }
    public static Map<String, ItemUseAbility> getAbilityMap(World world) {
        if (world.isClient) return abilityMapClient;
        return abilityMap;
    }

    public static ItemUseAbility getEmpty() {
        return emptyAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack) {
        return getAbility(itemStack, abilityMap);
    }

    private static ItemUseAbility getAbility(ItemStack itemStack, Map<String, ItemUseAbility> map) {
        ItemUseAbility useAbility = map.get(getCacheUUID(itemStack));
        return useAbility == null ? emptyAbility : useAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        for (ItemUseAbility ability : AbilityProperty.get(itemStack)) {
            if (ability.allowedOnItem(itemStack, world, player, hand)) {
                return ability;
            }
        }
        return emptyAbility;
    }

    public static UseAction getUseAction(ItemStack itemStack) {
        return getAbility(itemStack, abilityMapClient).getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack) {
        return getAbility(itemStack).getMaxUseTime(itemStack);
    }

    public static TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemUseAbility ability = getAbility(itemStack, world, user, hand);
        String uuid = getCacheUUID(itemStack);
        if (uuid != null) getAbilityMap(world).put(uuid, ability);

        trigger(new Ability(itemStack, world, user, null, ability), ABILITY_START);

        return ability.use(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemUseAbility ability = getAbility(stack);
        ItemStack itemStack = ability.finishUsing(stack, world, user);
        removeFromAbilityMap(stack, getAbilityMap(world));

        trigger(new Ability(itemStack, world, user, null, ability), ABILITY_FINISH, ABILITY_END);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        ability.onStoppedUsing(stack, world, user, remainingUseTicks);

        if (user instanceof PlayerEntity player) {
            Map<PlayerEntity, ItemStack> activeItems = playerActiveItems;
            if (player.getWorld().isClient)
                activeItems = playerActiveItemsClient;
            activeItems.put(player, ItemStack.EMPTY);
        }

        trigger(new Ability(stack, world, user, remainingUseTicks, ability), ABILITY_STOP, ABILITY_STOP_USING, ABILITY_END);

        removeFromAbilityMap(stack, getAbilityMap(world));
    }

    public static void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        trigger(new Ability(stack, world, user, remainingUseTicks, ability), ABILITY_TICK);
        ability.usageTick(world, user, stack, remainingUseTicks);
    }

    public static ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return getAbility(stack).useOnEntity(stack, user, entity, hand);
    }

    public static ActionResult useOnBlock(ItemUsageContext context) {
        return getAbility(context.getStack()).useOnBlock(context);
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
            return true;
        }

        @Override
        public UseAction getUseAction(ItemStack itemStack) {
            return UseAction.NONE;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack) {
            return 0;
        }

        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }
}
