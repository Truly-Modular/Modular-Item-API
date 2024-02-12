package smartin.miapi.modules.abilities.util;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

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
    private static final Map<ItemStack, ItemUseAbility> abilityMap = new WeakHashMap<>();

    public static void setup() {
        TickEvent.PLAYER_PRE.register((playerEntity) -> {
            Map<PlayerEntity, ItemStack> activeItems = playerActiveItems;
            if (playerEntity.getWorld().isClient)
                activeItems = playerActiveItemsClient;

            ItemStack oldItem = activeItems.get(playerEntity);
            ItemStack playerItem = playerEntity.getActiveItem();

            if (playerItem != null && !playerItem.equals(oldItem)) {
                activeItems.put(playerEntity, playerItem);
                if (oldItem != null) {
                    ItemUseAbility ability = getAbility(oldItem);
                    ability.onStoppedHolding(oldItem, playerEntity.getWorld(), playerEntity);
                    abilityMap.remove(oldItem);
                }
            }
        });
        useAbilityRegistry.register("empty", emptyAbility);
    }

    public static ItemUseAbility getEmpty() {
        return emptyAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack) {
        ItemUseAbility useAbility = abilityMap.get(itemStack);
        return useAbility == null ? emptyAbility : useAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack, World world, PlayerEntity player, Hand hand, AbilityHitContext abilityHitContext) {
        for (ItemUseAbility ability : AbilityProperty.get(itemStack)) {
            if (ability.allowedOnItem(itemStack, world, player, hand, abilityHitContext)) {
                return ability;
            }
        }
        return emptyAbility;
    }

    public static UseAction getUseAction(ItemStack itemStack) {
        return getAbility(itemStack).getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack) {
        return getAbility(itemStack).getMaxUseTime(itemStack);
    }

    public static TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemUseAbility ability = getAbility(itemStack, world, user, hand, new AbilityHitContext() {
            @Override
            public @Nullable ItemUsageContext hitResult() {
                return null;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return null;
            }
        });
        abilityMap.put(itemStack, ability);

        return ability.use(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemUseAbility ability = getAbility(stack);
        ItemStack itemStack = ability.finishUsing(stack, world, user);
        abilityMap.remove(stack);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        ability.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (ability instanceof ItemUseDefaultCooldownAbility itemUseDefaultCooldownAbility) {
            itemUseDefaultCooldownAbility.afterStopAbility(stack, world, user, remainingUseTicks);
        }
        abilityMap.remove(stack);
    }

    public static void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        ability.usageTick(world, user, stack, remainingUseTicks);
    }

    public static ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ItemUseAbility ability = getAbility(stack, user.getWorld(), user, hand, new AbilityHitContext() {
            @Override
            public @Nullable ItemUsageContext hitResult() {
                return null;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return entity;
            }
        });
        abilityMap.put(stack, ability);
        return getAbility(stack).useOnEntity(stack, user, entity, hand);
    }

    public static ActionResult useOnBlock(ItemUsageContext context) {
        ItemUseAbility ability = getAbility(context.getStack(), context.getWorld(), context.getPlayer(), context.getHand(), new AbilityHitContext() {
            @Override
            public @Nullable ItemUsageContext hitResult() {
                return context;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return null;
            }
        });
        abilityMap.put(context.getStack(), ability);
        return getAbility(context.getStack()).useOnBlock(context);
    }

    public interface AbilityHitContext {
        @Nullable
        ItemUsageContext hitResult();

        @Nullable
        Entity hitEntity();
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, AbilityHitContext abilityHitContext) {
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