package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.DynamicOps;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import smartin.miapi.Miapi;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.key.KeyBindFacet;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * The ItemAbilityManager is the brain and control behind what Ability is executed on what Item.
 * Abilities need to be added in the Module Json so the {@link AbilityMangerProperty} can pick them up properly
 * This class then checks all provided abilites and delegates the calls from the {@link net.minecraft.world.item.Item} to the {@link ItemUseAbility}
 */
public class ItemAbilityManager {
    private static final Map<Player, ItemStack> playerActiveItems = new WeakHashMap<>();
    private static final Map<Player, ItemStack> playerActiveItemsClient = new WeakHashMap<>();
    public static final MiapiRegistry<ItemUseAbility> useAbilityRegistry = MiapiRegistry.getInstance(ItemUseAbility.class);
    private static final AbilityHolder<?> emptyAbility = new AbilityHolder(new EmptyAbility(), new Object());
    private static final Map<ItemStack, AbilityHolder<?>> abilityMap = new WeakHashMap<>();
    public static final Map<Player, ResourceLocation> clientKeyBindID = new WeakHashMap<>();
    public static final Map<Player, ResourceLocation> serverKeyBindID = new WeakHashMap<>();

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
        useAbilityRegistry.register(Miapi.id("empty"), emptyAbility.ability());
    }

    public static AbilityHolder<?> getEmpty() {
        return emptyAbility;
    }

    private static AbilityHolder<?> getAbility(ItemStack itemStack) {
        AbilityHolder<?> useAbility = abilityMap.get(itemStack);
        return useAbility == null ? emptyAbility : useAbility;
    }

    private static AbilityHolder<?> getAbility(ItemStack itemStack, Level world, Player player, InteractionHand hand, AbilityHitContext abilityHitContext) {
        ResourceLocation keybindID = null;
        if (player.level().isClientSide) {
            keybindID = clientKeyBindID.get(player);
        } else {
            keybindID = serverKeyBindID.get(player);
        }
        if (keybindID == null) {
            for (Map.Entry<ItemUseAbility<?>, Object> entry : AbilityMangerProperty.property.getData(itemStack).orElse(new HashMap<>()).entrySet()) {
                if (entry.getKey().allowedOnItem(itemStack, world, player, hand, abilityHitContext)) {
                    //return new Pair<>(entry.getKey(), entry.getValue());
                    if (player instanceof ServerPlayer serverPlayer) {
                        KeyBindFacet.get(serverPlayer).reset(serverPlayer);
                    }
                    return entry.getKey().getAsHolder(entry.getValue());
                }
            }
        } else {
            var map = KeyBindAbilityManagerProperty.property.getData(itemStack).orElse(new HashMap<>());
            Map<ItemUseAbility<?>, Object> abilities = map.get(keybindID);
            if (abilities != null) {
                for (Map.Entry<ItemUseAbility<?>, Object> entry : abilities.entrySet()) {
                    if (entry.getKey().allowedOnItem(itemStack, world, player, hand, abilityHitContext)) {
                        //return new Pair<>(entry.getKey(), entry.getValue());
                        if (player instanceof ServerPlayer serverPlayer) {
                            if(KeyBindFacet.get(serverPlayer)!=null){
                                ShieldingArmorFacet facet;
                                KeyBindFacet.get(serverPlayer).set(keybindID, serverPlayer);
                            }
                        }
                        return entry.getKey().getAsHolder(entry.getValue());
                    }
                }
            }
        }
        return emptyAbility;
    }

    public static UseAnim getUseAction(ItemStack itemStack, Supplier<UseAnim> getItem) {
        AbilityHolder<?> ability = getAbility(itemStack);
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.ability().getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity, Supplier<Integer> getItem) {
        AbilityHolder<?> ability = getAbility(itemStack);
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.ability().getMaxUseTime(itemStack, livingEntity);
    }

    public static InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, Supplier<InteractionResultHolder<ItemStack>> getItem) {
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
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.ability().use(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user, Supplier<ItemStack> getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            abilityMap.remove(stack);
            return getItem.get();
        }
        ItemStack itemStack = ability.ability().finishUsing(stack, world, user);
        abilityMap.remove(stack);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, Runnable getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            getItem.run();
            return;
        }
        ability.ability().onStoppedUsing(stack, world, user, remainingUseTicks);
        if (ability.ability() instanceof ItemUseDefaultCooldownAbility itemUseDefaultCooldownAbility) {
            itemUseDefaultCooldownAbility.afterStopAbility(stack, world, user, remainingUseTicks);
        }
        abilityMap.remove(stack);
    }

    public static void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, Runnable getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            abilityMap.remove(stack);
            getItem.run();
            return;
        }
        ability.ability().usageTick(world, user, stack, remainingUseTicks);
    }

    public static boolean useOnRelease(ItemStack stack, Supplier<Boolean> getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            abilityMap.remove(stack);
            return getItem.get();
        }
        return ability.ability().useOnRelease(stack);
    }

    public static InteractionResult useOnEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand, Supplier<InteractionResult> getItem) {
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
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        abilityMap.put(stack, ability);
        return getAbility(stack).ability().useOnEntity(stack, user, entity, hand);
    }

    public static InteractionResult useOnBlock(UseOnContext context, Supplier<InteractionResult> getItem) {
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
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
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
        public int getMaxUseTime(ItemStack itemStack, LivingEntity entity) {
            return 0;
        }

        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }

        @Override
        public Object decode(DynamicOps ops, Object prefix) {
            return null;
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