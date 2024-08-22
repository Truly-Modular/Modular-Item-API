package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EnchantmentProperty;


@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @ModifyReturnValue(
            method = "isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z",
            at = @At("RETURN"))
    private boolean miapi$isAcceptableItemMixin(boolean original, ItemStack stack) {
        Enchantment enchantment = (Enchantment) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            boolean acceptable = EnchantmentProperty.isAllowed(stack, enchantment);
            if (MiapiConfig.INSTANCE.server.enchants.lenientEnchantments) {
                acceptable = acceptable || cir.getReturnValue();
            }
            return acceptable;
        }
        return original;
    }


}
