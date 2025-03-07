package smartin.miapi.fabric.mixin;

import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.fabric.MiapiReloadListener;

@Mixin(ReloadableServerResources.class)
public class ReloadAbleServerResourcesMixin {

    @Shadow
    @Final
    private ReloadableServerRegistries.Holder fullRegistryHolder;

    @Inject(method = "updateRegistryTags()V", at = @At(value = "TAIL"))
    private void miapi$getLastReloadCall(CallbackInfo ci) {
        MiapiReloadListener.actualReload(this.fullRegistryHolder.get());
    }
}
