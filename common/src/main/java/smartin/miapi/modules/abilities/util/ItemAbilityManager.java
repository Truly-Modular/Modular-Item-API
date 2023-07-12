package smartin.miapi.modules.abilities.util;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.*;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.modules.properties.util.event.PropertyApplication;
import smartin.miapi.registries.MiapiRegistry;

import java.util.*;

import static smartin.miapi.modules.properties.util.event.ApplicationEvent.*;
import static smartin.miapi.modules.properties.util.event.PropertyApplication.Ability;

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
                activeItems.put(playerEntity, playerEntity.getActiveItem());
                if (oldItem != null) {
                    ItemUseAbility ability = getAbility(oldItem);
                    if (ability != emptyAbility)
                        trigger(new Ability(oldItem, playerEntity.getWorld(), playerEntity, playerEntity.getItemUseTimeLeft(), ability), PropertyApplication.ABILITY_END, PropertyApplication.ABILITY_STOP, PropertyApplication.ABILITY_STOP_HOLDING);
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

    private static ItemUseAbility getAbility(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        LootManager manager = world.getServer() != null ? world.getServer().getLootManager() : null;
        System.out.println(AbilityProperty.getStatic(itemStack));
        for (Map.Entry<ItemUseAbility, Identifier> entry : AbilityProperty.getStatic(itemStack).entrySet()) {
            ItemUseAbility ability = entry.getKey();
            Identifier predicate = entry.getValue();

            System.out.println(ability + " is ab");
            System.out.println("pred: " + predicate);
            if (predicate != null && !predicate.getPath().equals(".none") && manager != null && world instanceof ServerWorld sw) {
                LootCondition condition = manager.getElement(LootDataType.PREDICATES, predicate);
                if (condition != null) {
                    LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(sw)
                            .add(LootContextParameters.THIS_ENTITY, player)
                            .add(LootContextParameters.ORIGIN, player.getPos());
                    if (!condition.test(new LootContext.Builder(builder.build(LootContextTypes.SELECTOR)).build(null))) {
                        System.out.println("continuing");
                        continue;
                    }
                } else
                    Miapi.LOGGER.warn("Found null predicate used for ItemUseAbility.");
            }

            if (ability.allowedOnItem(itemStack, world, player, hand)) {
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
        ItemUseAbility ability = getAbility(itemStack, world, user, hand);
        abilityMap.put(itemStack, ability);

        if (ability != emptyAbility)
            trigger(new Ability(itemStack, world, user, null, ability), PropertyApplication.ABILITY_START);

        return ability.use(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemUseAbility ability = getAbility(stack);
        ItemStack itemStack = ability.finishUsing(stack, world, user);
        abilityMap.remove(stack);

        if (ability != emptyAbility)
            trigger(new Ability(itemStack, world, user, null, ability), PropertyApplication.ABILITY_FINISH, PropertyApplication.ABILITY_END);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        ability.onStoppedUsing(stack, world, user, remainingUseTicks);

        if (ability != emptyAbility)
            trigger(new Ability(stack, world, user, remainingUseTicks, ability), PropertyApplication.ABILITY_STOP, PropertyApplication.ABILITY_STOP_USING, PropertyApplication.ABILITY_END);

        abilityMap.remove(stack);
    }

    public static void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemUseAbility ability = getAbility(stack);
        if (ability != emptyAbility)
            trigger(new Ability(stack, world, user, remainingUseTicks, ability), PropertyApplication.ABILITY_TICK);
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