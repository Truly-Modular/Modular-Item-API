package smartin.miapi.modules.abilities.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ItemUseDefaultCooldownAbility extends ItemUseAbility {


    default int getCooldown(ItemStack itemstack) {
        return getAbilityContext(itemstack).getInt("cooldown", getDefaultCooldown());
    }

    default boolean useCooldown(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (this instanceof ItemUseMinHoldAbility itemUseMinHoldAbility) {
            return itemUseMinHoldAbility.finishedMinHold(stack, world, user, remainingUseTicks);
        }
        return true;
    }

    default int getDefaultCooldown() {
        return 0;
    }

    default void afterStopAbility(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (useCooldown(stack, world, user, remainingUseTicks) && user instanceof Player player) {
            player.getCooldowns().addCooldown(stack.getItem(), getCooldown(stack));
        }
    }
}
