package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
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
import smartin.miapi.modules.abilities.key.KeyBindAbilityManagerProperty;
import smartin.miapi.modules.abilities.key.KeyBindFacet;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
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
    private static final Map<ItemStack, AbilityHolder<?>> abilityMap = Collections.synchronizedMap(new WeakHashMap<>());
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
                    holder.onStoppedHolding(oldItem, playerEntity.level(), playerEntity);
                    clearAbility(oldItem);
                }
            }
        });
        useAbilityRegistry.addCallback(ability -> ModularItemCache.setSupplier(
                AbilityMangerProperty.KEY + "_" + RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(ability),
                (itemStack -> {
                    AbilityHolder<?> optional = abilityMap.get(itemStack);
                    if (optional != null && optional.ability().equals(ability)) {
                        return optional.context();
                    }
                    return null;
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

    public static List<AbilityHolder<?>> getAbilities(ItemStack itemStack) {
        List<AbilityHolder<?>> result = new ArrayList<>();

        // Abilities without keybinds
        AbilityProperty.property.getData(itemStack).ifPresent(list -> {
            list.forEach(context -> {
                result.add(context.ability.getAsHolder(context.data));
            });
        });

        // Abilities with keybinds
        Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> keyboundAbilities =
                KeyBindAbilityManagerProperty.property.getData(itemStack).orElse(new HashMap<>());
        for (List<AbilityProperty.AbilityContext<?>> list : keyboundAbilities.values()) {
            list.forEach(context -> {
                result.add(context.ability.getAsHolder(context.data));
            });
        }

        return result;
    }


    private static AbilityHolder<?> getAbility(ItemStack itemStack, Level world, Player player, InteractionHand hand, AbilityHitContext abilityHitContext) {
        ResourceLocation keybindID = null;
        if (player.level().isClientSide) {
            keybindID = clientKeyBindID.get(player);
        } else {
            keybindID = serverKeyBindID.get(player);
        }
        if (keybindID == null) {
            AbilityHolder<?> abilityHolder =
                    AbilityProperty.property.getData(itemStack)
                            .flatMap(list -> list.stream()
                                    .filter(abilityContext -> {
                                        if (
                                                abilityHitContext.hitEntity() != null &&
                                                abilityContext.allowedOnEntity.isTrue()) {
                                            return true;
                                        } else if (abilityContext.allowedOnBlock.isTrue() &&
                                                   abilityHitContext.hitEntity() == null &&
                                                   abilityHitContext.hitResult() != null &&
                                                   abilityHitContext.hitResult().getClickedPos() != null
                                        ) {
                                            return true;
                                        }
                                        return abilityContext.allowedOnAir.isTrue() &&
                                               abilityHitContext.hitEntity() == null &&
                                               abilityHitContext.hitResult() == null;
                                    })
                                    .map(abilityContext -> abilityContext.ability.getAsHolder(abilityContext.data))
                                    .filter(ability -> ability.allowedOnItem(itemStack, world, player, hand, abilityHitContext))
                                    .findFirst()
                            ).orElse(null);

            if (abilityHolder != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    KeyBindFacet.get(serverPlayer).reset(serverPlayer);
                }
                setCurrentAbility(itemStack, abilityHolder);
                return abilityHolder;
            }
        } else {
            var map = KeyBindAbilityManagerProperty.property.getData(itemStack).orElse(new LinkedHashMap<>());
            AbilityHolder<?> abilityHolder =
                    Optional.ofNullable(map.get(keybindID))
                            .flatMap(list -> list.stream()
                                    .filter(abilityContext -> {
                                        if (
                                                abilityHitContext.hitEntity() != null &&
                                                abilityContext.allowedOnEntity.isTrue()) {
                                            return true;
                                        } else if (abilityContext.allowedOnBlock.isTrue() &&
                                                   abilityHitContext.hitEntity() == null &&
                                                   abilityHitContext.hitResult() != null &&
                                                   abilityHitContext.hitResult().getClickedPos() != null
                                        ) {
                                            return true;
                                        }
                                        return abilityContext.allowedOnAir.isTrue() &&
                                               abilityHitContext.hitEntity() == null &&
                                               abilityHitContext.hitResult() == null;
                                    })
                                    .map(abilityContext -> abilityContext.ability.getAsHolder(abilityContext.data))
                                    .filter(ability -> ability.allowedOnItem(itemStack, world, player, hand, abilityHitContext))
                                    .findFirst()
                            ).orElse(null);

            if (abilityHolder != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    KeyBindFacet.get(serverPlayer).reset(serverPlayer);
                }
                setCurrentAbility(itemStack, abilityHolder);
                return abilityHolder;
            }
        }
        clearAbility(itemStack);
        return emptyAbility;
    }

    private static @Nullable AbilityHolder<?> setCurrentAbility(ItemStack itemStack, AbilityHolder<?> abilityHolder) {
        ModularItemCache.clear(itemStack,
                AbilityMangerProperty.KEY + "_" + RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(abilityHolder.ability()));
        return abilityMap.put(itemStack, abilityHolder);
    }

    private static AbilityHolder<?> clearAbility(ItemStack oldItem) {
        var abilityHolder = abilityMap.remove(oldItem);
        if (abilityHolder != null) {
            ModularItemCache.clear(oldItem,
                    AbilityMangerProperty.KEY + "_" + RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(abilityHolder.ability()));
        }
        return abilityHolder;
    }

    public static UseAnim getUseAction(ItemStack itemStack, Supplier<UseAnim> getItem) {
        AbilityHolder<?> ability = getAbility(itemStack);
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity, Supplier<Integer> getItem) {
        AbilityHolder<?> ability = getAbility(itemStack);
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.getMaxUseTime(itemStack, livingEntity);
    }

    public static InteractionResultHolder<ItemStack> use
            (Level world, Player user, InteractionHand hand, Supplier<InteractionResultHolder<ItemStack>> getItem) {
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
        setCurrentAbility(itemStack, ability);
        if (emptyAbility.equals(ability)) {
            return getItem.get();
        }
        return ability.use(world, user, hand);
    }

    public static ItemStack finishUsing
            (ItemStack stack, Level world, LivingEntity user, Supplier<ItemStack> getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            clearAbility(stack);
            return getItem.get();
        }
        ItemStack itemStack = ability.finishUsing(stack, world, user);
        clearAbility(stack);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, Level world, LivingEntity user,
                                      int remainingUseTicks, Runnable getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            getItem.run();
            return;
        }
        ability.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (ability.ability() instanceof ItemUseDefaultCooldownAbility itemUseDefaultCooldownAbility) {
            itemUseDefaultCooldownAbility.afterStopAbility(stack, world, user, remainingUseTicks);
        }
        clearAbility(stack);
    }

    public static void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, Runnable
            getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            clearAbility(stack);
            getItem.run();
            return;
        }
        ability.usageTick(world, user, stack, remainingUseTicks);
    }

    public static boolean useOnRelease(ItemStack stack, Supplier<Boolean> getItem) {
        AbilityHolder<?> ability = getAbility(stack);
        if (emptyAbility.equals(ability)) {
            clearAbility(stack);
            return getItem.get();
        }
        return ability.useOnRelease(stack);
    }

    public static InteractionResult useOnEntity
            (ItemStack stack, Player user, LivingEntity entity, InteractionHand hand, Supplier<InteractionResult> getItem) {
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
        setCurrentAbility(stack, ability);
        return getAbility(stack).useOnEntity(stack, user, entity, hand);
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
        setCurrentAbility(context.getItemInHand(), ability);
        if (Platform.isForgeLike()) {
            //fuck you forge, implemented fallback for ItemAbility shit
            InteractionResult result = getAbility(context.getItemInHand()).useOnBlock(context);
            if (result.equals(InteractionResult.PASS)) {
                return getItem.get();
            }
            return result;
        }
        AbilityHolder<?> executing = getAbility(context.getItemInHand());
        return executing.useOnBlock(context);
    }

    public interface AbilityHitContext {
        @Nullable
        UseOnContext hitResult();

        @Nullable
        Entity hitEntity();
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, AbilityHitContext abilityHitContext, Object context) {
            return true;
        }

        @Override
        public UseAnim getUseAction(ItemStack itemStack, Object context) {
            return UseAnim.NONE;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack, LivingEntity entity, Object context) {
            return 0;
        }

        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, Object context) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }

        @Override
        public Codec getCodec() {
            return AutoCodec.of(EmptyAbility.class).codec();
        }
    }

}