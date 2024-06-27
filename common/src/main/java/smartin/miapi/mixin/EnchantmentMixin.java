package smartin.miapi.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EnchantmentProperty;


@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", at = @At("TAIL"), cancellable = true)
    private void miapi$isAcceptableItemMixin(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment enchantment = (Enchantment) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            boolean acceptable = EnchantmentProperty.isAllowed(stack, enchantment);
            if (MiapiConfig.INSTANCE.server.enchants.lenientEnchantments) {
                acceptable = acceptable || cir.getReturnValue();
            }
            cir.setReturnValue(acceptable);
        }
    }


}
