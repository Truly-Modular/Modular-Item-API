package smartin.miapi.modules.abilities;

import com.mojang.serialization.DynamicOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;

//TODO: maybe implement cooldown, cooldown factor on hit and min hold time before shield up.
//i have no use for this so idgf
public class ShieldBlockAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return false;
    }

    public UseAnim getUseAction(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public Object getDefaultContext() {
        return null;
    }

    @Override
    public Object decode(DynamicOps ops, Object prefix) {
        return null;
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return 0;
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return 0;
    }
}
