package smartin.miapi.mixin;


import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.modules.properties.compat.BetterCombatHelper;

@Mixin(WeaponRegistry.class)
public class BCWeaponRegistryMixin {
    @Inject(
            method = "getAttributes(Lnet/minecraft/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = -1)
    private static void getAttributes(ItemStack itemStack, CallbackInfoReturnable<WeaponAttributes> cir) {
        WeaponAttributes attributes = BetterCombatHelper.getAttributes(itemStack);
        if (attributes != null) {
            cir.setReturnValue(attributes);
        }
    }
}
