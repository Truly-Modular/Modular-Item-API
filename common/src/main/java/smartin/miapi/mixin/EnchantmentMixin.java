package smartin.miapi.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EnchantmentProperty;


@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void isAcceptableItemMixin(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment enchantment = (Enchantment) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            boolean acceptable = EnchantmentProperty.isAllowed(stack, enchantment);
            cir.setReturnValue(acceptable);
        }
    }

}
