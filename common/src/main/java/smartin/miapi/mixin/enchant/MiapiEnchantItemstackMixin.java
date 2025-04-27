package smartin.miapi.mixin.enchant;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.modules.properties.enchanment.ItemEnchantmentsAccessor;

@Mixin(ItemStack.class)
public class MiapiEnchantItemstackMixin {

    @Inject(method = {"Lnet/minecraft/world/item/ItemStack;getComponents()Lnet/minecraft/core/component/DataComponentMap;"}, at = @At("RETURN"))
    private void afterModifyEnchantments(CallbackInfoReturnable<DataComponentMap> cir) {
        if (cir.getReturnValue().has(DataComponents.ENCHANTMENTS)) {
            ((ItemEnchantmentsAccessor) cir.getReturnValue().get(DataComponents.ENCHANTMENTS)).setOwnerStack((ItemStack) (Object) this);
        }
    }
}
