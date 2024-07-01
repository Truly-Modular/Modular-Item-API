package smartin.miapi.mixin;


import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.WeaponAttributesHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeaponAttributesHelper.class)
public class BCWeaponRegistryMixin {
    @Inject(
            method = "readFromNBT",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    private static void miapi$getAttributes(ItemStack itemStack, CallbackInfoReturnable<WeaponAttributes> cir) {
        /*
        if (itemStack.getItem() instanceof ModularItem && itemStack.hasNbt() && !itemStack.getOrCreateNbt().contains("weapon_attributes")) {
            WeaponAttributes attributes = BetterCombatHelper.getAttributes(itemStack);
            if (attributes != null) {
                cir.setReturnValue(attributes);
            }
        }
        */
    }
}