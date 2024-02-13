package smartin.miapi.modules.abilities.util;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.AbilityMangerProperty;

/**
 * Implement this interface to provide custom behavior for item usage and handling.
 * This class allows Modular Items to swap their Actions on RightClick (UseKey).
 * Register implementations in the {@link ItemAbilityManager#useAbilityRegistry} to be used.
 */
public interface ItemUseAbility<T> {
    /**
     * Checks if this {@link ItemUseAbility} is allowed on the specified item stack, world, player, and hand.
     *
     * @param itemStack The item stack being used.
     * @param world     The world in which the item is being used.
     * @param player    The player using the item.
     * @param hand      The hand with which the item is being used.
     * @return true if the item is allowed to be used, false otherwise.
     */
    boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext);

    /**
     * Gets the use action of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The use action of the item stack.
     */
    UseAction getUseAction(ItemStack itemStack);

    /**
     * Gets the maximum use time of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The maximum use time of the item stack.
     */
    int getMaxUseTime(ItemStack itemStack);

    /**
     * Handles the usage of the item in the specified world by the specified player and hand.
     * This is called when the item is first used, so the moment the user right clicks.
     *
     * @param world The world in which the item is being used.
     * @param user  The player using the item.
     * @param hand  The hand with which the item is being used.
     * @return The result of using the item, including the modified item stack.
     */
    TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    /**
     * Called when the item usage is finished (MaxUseTime is over)
     *
     * @param stack The item stack being used.
     * @param world The world in which the item was used.
     * @param user  The entity using the item.
     * @return The resulting item stack after finishing usage.
     */
    default ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return stack;
    }

    /**
     * Called when the item usage is stopped (stopped holding Left Click)
     *
     * @param stack             The item stack being used.
     * @param world             The world in which the item was used.
     * @param user              The entity using the item.
     * @param remainingUseTicks The remaining ticks of item usage.
     */
    default void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {

    }

    /**
     * Called when the player swaps or drops the item or for whatever other reason does nolonger hold the item.
     *
     * @param stack The item stack being held.
     * @param world The world in which the item is being held.
     * @param user  The entity holding the item.
     */
    default void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {

    }

    /**
     * Handles the usage of the item on an entity.
     *
     * @param stack  The item stack being used.
     * @param user   The player using the item.
     * @param entity The entity being interacted with.
     * @param hand   The hand with which the item is being used.
     * @return The result of using the item on the entity.
     */
    default ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ActionResult.PASS;
    }

    /**
     * Handles the usage of the item on a block.
     *
     * @param context The item usage context, including the item stack, player, and block information.
     * @return The result of using the item on the block.
     */
    default ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    default void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {

    }

    default AbilityMangerProperty.AbilityContext getAbilityContext(ItemStack itemStack) {
        AbilityMangerProperty.AbilityContext context = new AbilityMangerProperty.AbilityContext(new JsonObject(), ItemModule.getModules(itemStack), itemStack);
        String key = ItemAbilityManager.useAbilityRegistry.findKey(this);
        if (key != null) {
            context = AbilityMangerProperty.getContext(itemStack, key);
        }
        return context;
    }

    /**
     * This can be implemented for caching related context, abilities should use this if they are highly complex
     * @param jsonObject
     * @return
     */
    default T fromJson(JsonObject jsonObject) {
        return null;
    }

    default T getSpecialContext(ItemStack itemStack, T defaultValue) {
        return ModularItemCache.get(itemStack, AbilityMangerProperty.KEY + "_" + ItemAbilityManager.useAbilityRegistry.findKey(this), defaultValue);
    }
}
