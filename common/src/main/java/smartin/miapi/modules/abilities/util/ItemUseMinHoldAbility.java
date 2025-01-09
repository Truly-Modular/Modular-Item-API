package smartin.miapi.modules.abilities.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ItemUseMinHoldAbility<T> extends ItemUseAbility<T> {

    /**
     * The default min hold Time, defaults to if noone is defined in json
     *
     * @return
     */
    default int minHoldTimeDefault() {
        return 0;
    }

    default int getMinHoldTime(ItemStack itemStack) {
        return getAbilityContext(itemStack).getInt("min_hold", minHoldTimeDefault());
    }

    @Override
    default void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (finishedMinHold(stack, world, user, remainingUseTicks)) {
            onStoppedUsingAfter(stack, world, user, remainingUseTicks);
        }
    }

    default boolean finishedMinHold(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
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
    default void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {

    }
}
