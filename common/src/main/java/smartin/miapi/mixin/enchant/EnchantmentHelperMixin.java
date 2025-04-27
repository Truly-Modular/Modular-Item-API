package smartin.miapi.mixin.enchant;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
/*
    @Inject(method = {"getComponentType(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/component/DataComponentType;"}, at = @At("RETURN"))
    private static void afterModifyEnchantments(ItemStack context, CallbackInfoReturnable<DataComponentType<ItemEnchantments>> cir) {
        ((ItemEnchantmentsAccessor) cir.getReturnValue()).setOwnerStack(context);
    }

 */
}
