package smartin.miapi.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.Miapi;

@Mixin(TagManager.class)
abstract class TagManagerMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/core/RegistryAccess;)V",
            at = @At("RETURN"))
    private void miapi$captureRegistryAccess(RegistryAccess registryAccess, CallbackInfo ci) {
        Miapi.registryAccess = registryAccess;
    }
}
