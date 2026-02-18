package smartin.miapi.modules.abilities.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

/**
 * Implement this interface to provide custom behavior for item usage and handling.
 * This class allows Modular Items to swap their Actions on RightClick (UseKey).
 * Register implementations in the {@link ItemAbilityManager#useAbilityRegistry} to be used.
 */
public interface ItemUseAbility<T> extends MergeAble<T>, InitializeAble<T> {
    /**
     * Checks if this {@link ItemUseAbility} is allowed on the specified item stack, world, player, and hand.
     *
     * @param itemStack The item stack being used.
     * @param world     The world in which the item is being used.
     * @param player    The player using the item.
     * @param hand      The hand with which the item is being used.
     * @param context
     * @return true if the item is allowed to be used, false otherwise.
     */
    boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, T context);

    /**
     * Gets the use action of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @param context
     * @return The use action of the item stack.
     */
    UseAnim getUseAction(ItemStack itemStack, T context);

    /**
     * Gets the maximum use time of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @param context
     * @return The maximum use time of the item stack.
     */
    int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity, T context);

    /**
     * Handles the usage of the item in the specified world by the specified player and hand.
     * This is called when the item is first used, so the moment the user right clicks.
     *
     * @param world   The world in which the item is being used.
     * @param user    The player using the item.
     * @param hand    The hand with which the item is being used.
     * @param context
     * @return The result of using the item, including the modified item stack.
     */
    InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, T context);

    /**
     * Called when the item usage is finished (MaxUseTime is over)
     *
     * @param stack   The item stack being used.
     * @param world   The world in which the item was used.
     * @param user    The entity using the item.
     * @param context
     * @return The resulting item stack after finishing usage.
     */
    default ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user, T context) {
        return stack;
    }

    /**
     * Called when the item usage is stopped (stopped holding Left Click)
     *
     * @param stack             The item stack being used.
     * @param world             The world in which the item was used.
     * @param user              The entity using the item.
     * @param remainingUseTicks The remaining ticks of item usage.
     * @param context
     */
    default void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, T context) {

    }

    default boolean useOnRelease(ItemStack itemStack, T context) {
        return false;
    }

    /**
     * Called when the player swaps or drops the item or for whatever other reason does nolonger hold the item.
     *
     * @param stack   The item stack being held.
     * @param world   The world in which the item is being held.
     * @param user    The entity holding the item.
     * @param context
     */
    default void onStoppedHolding(ItemStack stack, Level world, LivingEntity user, T context) {

    }

    default EquipmentSlot getEquipmentSlot(InteractionHand hand) {
        return hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    /**
     * Handles the usage of the item on an entity.
     *
     * @param stack   The item stack being used.
     * @param user    The player using the item.
     * @param entity  The entity being interacted with.
     * @param hand    The hand with which the item is being used.
     * @param context
     * @return The result of using the item on the entity.
     */
    default InteractionResult useOnEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand, T context) {
        return InteractionResult.PASS;
    }

    /**
     * Handles the usage of the item on a block.
     *
     * @param context        The item usage context, including the item stack, player, and block information.
     * @param abilityContext
     * @return The result of using the item on the block.
     */
    default InteractionResult useOnBlock(UseOnContext context, T abilityContext) {
        return InteractionResult.PASS;
    }

    default void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, T context) {

    }

    Codec<T> getCodec();

    default <K> T decode(DynamicOps<K> ops, K prefix) {
        return getCodec().decode(ops, prefix).getOrThrow(s ->
                new DecoderException("Could not decode Ability " + getClass().getName() + " with error" + s)).getFirst();
    }

    default <K> K encode(DynamicOps<K> ops, T input) {
        return getCodec().encodeStart(ops, input).getOrThrow(s ->
                new DecoderException("Could not encode Ability " + getClass().getName() + " with error" + s));
    }

    default T getDefaultContext() {
        return null;
    }

    default <K> K encodeObject(DynamicOps<K> ops, Object input) {
        return encode(ops, castTo(input));
    }

    default T initialize(T data, ModuleInstance moduleInstance) {
        return data;
    }

    default T merge(T left, T right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Nullable
    default T getSpecialContext(ItemStack itemStack) {
        return getSpecialContext(itemStack, getDefaultContext());
    }

    @Nullable
    default T getSpecialContext(ItemStack itemStack, T defaultValue) {
        return ModularItemCache.get(
                itemStack,
                AbilityMangerProperty.KEY + "_" + RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(this), defaultValue);
    }

    @SuppressWarnings("unchecked")
    default T castTo(Object object) {
        return (T) object;
    }

    @SuppressWarnings("unchecked")
    default AbilityHolder<T> getAsHolder(Object context) {
        return new AbilityHolder<>(this, (T) context);
    }


}
