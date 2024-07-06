package smartin.miapi.modules.abilities.util;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * The ItemAbilityManager is the brain and control behind what Ability is executed on what Item.
 * Abilities need to be added in the Module Json so the {@link AbilityMangerProperty} can pick them up properly
 * This class then checks all provided abilites and delegates the calls from the {@link net.minecraft.world.item.Item} to the {@link ItemUseAbility}
 */
public class ItemAbilityManager {
    private static final Map<Player, ItemStack> playerActiveItems = new HashMap<>();
    private static final Map<Player, ItemStack> playerActiveItemsClient = new HashMap<>();
    public static final MiapiRegistry<ItemUseAbility> useAbilityRegistry = MiapiRegistry.getInstance(ItemUseAbility.class);
    private static final AbilityHolder<?> emptyAbility = new AbilityHolder(new EmptyAbility(), new Object());
    private static final Map<ItemStack, AbilityHolder<?>> abilityMap = new WeakHashMap<>();

    public static void setup() {
        TickEvent.PLAYER_PRE.register((playerEntity) -> {
            Map<Player, ItemStack> activeItems = playerActiveItems;
            if (playerEntity.level().isClientSide)
                activeItems = playerActiveItemsClient;

            ItemStack oldItem = activeItems.get(playerEntity);
            ItemStack playerItem = playerEntity.getUseItem();

            if (playerItem != null && !playerItem.equals(oldItem)) {
                activeItems.put(playerEntity, playerItem);
                if (oldItem != null) {
                    AbilityHolder<?> holder = getAbility(oldItem);
                    holder.ability().onStoppedHolding(oldItem, playerEntity.level(), playerEntity);
                    abilityMap.remove(oldItem);
                }
            }
        });
        useAbilityRegistry.addCallback(ability -> ModularItemCache.setSupplier(
                AbilityMangerProperty.KEY + "_" + ItemAbilityManager.useAbilityRegistry.findKey(ability),
                (itemStack -> {
                    Optional<AbilityHolder<?>> optional = abilityMap.values().stream().filter(e -> e.ability().equals(ability)).findFirst();
                    return optional.<Object>map(AbilityHolder::context).orElse(null);
                })));
        useAbilityRegistry.register("empty", emptyAbility.ability());
    }

    public static AbilityHolder<?> getEmpty() {
        return emptyAbility;
    }

    private static AbilityHolder<?> getAbility(ItemStack itemStack) {
        AbilityHolder<?> useAbility = abilityMap.get(itemStack);
        return useAbility == null ? emptyAbility : useAbility;
    }

    private static AbilityHolder<?> getAbility(ItemStack itemStack, Level world, Player player, InteractionHand hand, AbilityHitContext abilityHitContext) {
        AbilityMangerProperty.property.getProperty(itemStack);
        for (Map.Entry<ItemUseAbility<?>, Object> entry : AbilityMangerProperty.property.getProperty(itemStack).entrySet()) {
            if (entry.getKey().allowedOnItem(itemStack, world, player, hand, abilityHitContext)) {
                //return new Pair<>(entry.getKey(), entry.getValue());
                return new AbilityHolder<>(entry.getKey(), entry.getKey().castTo(entry.getValue()));
            }
        }
        return emptyAbility;
    }

    public static UseAnim getUseAction(ItemStack itemStack) {
        return getAbility(itemStack).ability().getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return getAbility(itemStack).ability().getMaxUseTime(itemStack);
    }

    public static InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        AbilityHolder<?> ability = getAbility(itemStack, world, user, hand, new AbilityHitContext() {
            @Override
            public @Nullable UseOnContext hitResult() {
                return null;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return null;
            }
        });
        abilityMap.put(itemStack, ability);

        return ability.ability().use(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        AbilityHolder<?> ability = getAbility(stack);
        ItemStack itemStack = ability.ability().finishUsing(stack, world, user);
        abilityMap.remove(stack);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        AbilityHolder<?> ability = getAbility(stack);
        ability.ability().onStoppedUsing(stack, world, user, remainingUseTicks);
        if (ability.ability() instanceof ItemUseDefaultCooldownAbility itemUseDefaultCooldownAbility) {
            itemUseDefaultCooldownAbility.afterStopAbility(stack, world, user, remainingUseTicks);
        }
        abilityMap.remove(stack);
    }

    public static void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        getAbility(stack).ability().usageTick(world, user, stack, remainingUseTicks);
    }

    public static InteractionResult useOnEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        AbilityHolder<?> ability = getAbility(stack, user.level(), user, hand, new AbilityHitContext() {
            @Override
            public @Nullable UseOnContext hitResult() {
                return null;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return entity;
            }
        });
        abilityMap.put(stack, ability);
        return getAbility(stack).ability().useOnEntity(stack, user, entity, hand);
    }

    public static InteractionResult useOnBlock(UseOnContext context) {
        AbilityHolder<?> ability = getAbility(context.getItemInHand(), context.getLevel(), context.getPlayer(), context.getHand(), new AbilityHitContext() {
            @Override
            public @Nullable UseOnContext hitResult() {
                return context;
            }

            @Override
            public @Nullable Entity hitEntity() {
                return null;
            }
        });
        abilityMap.put(context.getItemInHand(), ability);
        return getAbility(context.getItemInHand()).ability().useOnBlock(context);
    }

    public interface AbilityHitContext {
        @Nullable
        UseOnContext hitResult();

        @Nullable
        Entity hitEntity();
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, AbilityHitContext abilityHitContext) {
            return true;
        }

        @Override
        public UseAnim getUseAction(ItemStack itemStack) {
            return UseAnim.NONE;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack) {
            return 0;
        }

        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }

        @Override
        public Object getDefaultContext() {
            return null;
        }
    }

    public record AbilityHolder<T>(ItemUseAbility<T> ability, T context) {

        public AbilityHolder(Object context, ItemUseAbility<T> ability) {
            this(ability, ability.castTo(context));
        }

    }
}