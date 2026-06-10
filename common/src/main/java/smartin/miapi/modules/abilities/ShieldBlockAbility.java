package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
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


//i have no use for this so idgf
public class ShieldBlockAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, Object context) {
        return false;
    }

    public UseAnim getUseAction(ItemStack stack, Object context) {
        return UseAnim.BLOCK;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity entity, Object context) {
        return 72000;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, Object context) {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public Codec getCodec() {
        return AutoCodec.of(ShieldBlockAbility.class).codec();
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
