package smartin.miapi.modules.abilities.util;

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

public record AbilityHolder<T>(ItemUseAbility<T> ability, T context) {

    public AbilityHolder(Object context, ItemUseAbility<T> ability) {
        this(ability, ability.castTo(context));
    }

    /**
     * Checks if this {@link ItemUseAbility} is allowed on the specified item stack, world, player, and hand.
     *
     * @param itemStack The item stack being used.
     * @param world     The world in which the item is being used.
     * @param player    The player using the item.
     * @param hand      The hand with which the item is being used.
     * @return true if the item is allowed to be used, false otherwise.
     */
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return ability().allowedOnItem(itemStack, world, player, hand, abilityHitContext, context());
    }

    /**
     * Gets the use action of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The use action of the item stack.
     */
    public UseAnim getUseAction(ItemStack itemStack) {
        return ability().getUseAction(itemStack, context());
    }

    /**
     * Gets the maximum use time of the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The maximum use time of the item stack.
     */
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return ability().getMaxUseTime(itemStack, livingEntity, context());
    }

    /**
     * Handles the usage of the item in the specified world by the specified player and hand.
     * This is called when the item is first used, so the moment the user right clicks.
     *
     * @param world The world in which the item is being used.
     * @param user  The player using the item.
     * @param hand  The hand with which the item is being used.
     * @return The result of using the item, including the modified item stack.
     */
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ability().use(world, user, hand, context());
    }

    /**
     * Called when the item usage is finished (MaxUseTime is over)
     *
     * @param stack The item stack being used.
     * @param world The world in which the item was used.
     * @param user  The entity using the item.
     * @return The resulting item stack after finishing usage.
     */
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        return ability().finishUsing(stack, world, user, context());
    }

    /**
     * Called when the item usage is stopped (stopped holding Left Click)
     *
     * @param stack             The item stack being used.
     * @param world             The world in which the item was used.
     * @param user              The entity using the item.
     * @param remainingUseTicks The remaining ticks of item usage.
     */
    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        ability().onStoppedUsing(stack, world, user, remainingUseTicks, context());
    }


    public boolean useOnRelease(ItemStack itemStack) {
        return ability().useOnRelease(itemStack, context());
    }

    /**
     * Called when the player swaps or drops the item or for whatever other reason does nolonger hold the item.
     *
     * @param stack The item stack being held.
     * @param world The world in which the item is being held.
     * @param user  The entity holding the item.
     */
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {

    }

    public EquipmentSlot getEquipmentSlot(InteractionHand hand) {
        return ability().getEquipmentSlot(hand);
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
    public InteractionResult useOnEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return ability().useOnEntity(stack, user, entity, hand, context());
    }

    /**
     * Handles the usage of the item on a block.
     *
     * @param context The item usage context, including the item stack, player, and block information.
     * @return The result of using the item on the block.
     */
    public InteractionResult useOnBlock(UseOnContext context) {
        return ability().useOnBlock(context, context());
    }

    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ability().usageTick(world, user, stack, remainingUseTicks, context());
    }
}
