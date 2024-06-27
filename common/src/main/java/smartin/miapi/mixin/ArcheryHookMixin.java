package smartin.miapi.mixin;


import net.archers.item.misc.AutoFireHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;

@Mixin(AutoFireHook.class)
public class ArcheryHookMixin {
    @Inject(
            method = "isApplicable(Lnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    private static void miapi$removeHook(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        /**
         * Archery implements this poorly and
         * causes an infinite loop and
         * has no native way of blacklisting crossbows
         */
        if (itemStack.getItem() instanceof ModularItem) {
            cir.setReturnValue(false);
        }
    }
}
