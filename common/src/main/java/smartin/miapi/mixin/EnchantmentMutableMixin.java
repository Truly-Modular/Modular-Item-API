package smartin.miapi.mixin;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.MixinContextFlags;

@Mixin(ItemEnchantments.Mutable.class)
public class EnchantmentMutableMixin {
    @Inject(method = {"set", "upgrade", "removeIf"}, at = @At("HEAD"))
    private void onModifyEnchantments(CallbackInfo ci) {
        MixinContextFlags.CALLED_FROM_MUTABLE.set(true);
    }

    @Inject(method = {"set", "upgrade", "removeIf"}, at = @At("RETURN"))
    private void afterModifyEnchantments(CallbackInfo ci) {
        MixinContextFlags.CALLED_FROM_MUTABLE.set(false);
    }
}
