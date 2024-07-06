package smartin.miapi.modules.abilities.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ItemUseMinHoldAbility<T> extends ItemUseAbility<T> {


     int getMinHoldTime(ItemStack itemStack);

    @Override
    default void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (finishedMinHold(stack, world, user, remainingUseTicks)) {
            onStoppedUsingAfter(stack, world, user, remainingUseTicks);
        }
    }

    default boolean finishedMinHold(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        return getMaxUseTime(stack) - remainingUseTicks > getMinHoldTime(stack);
    }

    /**
     * Called when the item usage is stopped (stopped holding Left Click) and it has been held for the Minimum Hold Time
     *
     * @param stack             The item stack being used.
     * @param world             The world in which the item was used.
     * @param user              The entity using the item.
     * @param remainingUseTicks The remaining ticks of item usage.
     */
    default void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {

    }
}
