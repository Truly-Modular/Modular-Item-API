package smartin.miapi.mixin.enchant;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = {"Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"}, at = @At("RETURN"))
    private static void afterModifyEnchantments(ServerLevel level, ItemStack stack, int damage, CallbackInfoReturnable<Integer> cir) {
        if (ModularItem.isModularItem(stack)) {
            MiapiEvents.MODULAR_ITEM_DAMAGE.invoker().durability(damage,stack,level);
        }
    }
}
