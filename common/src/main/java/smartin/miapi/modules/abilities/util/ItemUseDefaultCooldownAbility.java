package smartin.miapi.modules.abilities.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ItemUseDefaultCooldownAbility<T> extends ItemUseAbility<T> {


    default int getCooldown(ItemStack itemstack) {
        return getAbilityContext(itemstack).getInt("cooldown", getDefaultCooldown());
    }

    default boolean useCooldown(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (this instanceof ItemUseMinHoldAbility itemUseMinHoldAbility) {
            return itemUseMinHoldAbility.finishedMinHold(stack, world, user, remainingUseTicks);
        }
        return true;
    }

    default int getDefaultCooldown() {
        return 0;
    }

    default void afterStopAbility(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (useCooldown(stack, world, user, remainingUseTicks) && user instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(stack.getItem(), getCooldown(stack));
        }
    }
}
