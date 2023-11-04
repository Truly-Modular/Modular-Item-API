package smartin.miapi.mixin;


import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.WeaponAttributesHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.compat.BetterCombatHelper;

@Mixin(WeaponAttributesHelper.class)
public class BCWeaponRegistryMixin {
    @Inject(
            method = "Lnet/bettercombat/api/WeaponAttributesHelper;readFromNBT(Lnet/minecraft/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    private static void miapi$getAttributes(ItemStack itemStack, CallbackInfoReturnable<WeaponAttributes> cir) {
        if(itemStack.getItem() instanceof ModularItem && !itemStack.getOrCreateNbt().contains("weapon_attributes")){
            WeaponAttributes attributes = BetterCombatHelper.getAttributes(itemStack);
            if (attributes != null) {
                cir.setReturnValue(attributes);
            }
        }
    }
}
