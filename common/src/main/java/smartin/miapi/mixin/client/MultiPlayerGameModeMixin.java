package smartin.miapi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.modules.abilities.key.handler.UseItemAbilityHandler;

@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true)
    private void injectMethod(CallbackInfo ci) {
        /*
        ReloadHandlerBuilder
                .builder("miapi/key_binding")
                .handler((isClient, id, data, registryAccess) -> KeyBindManager.processKeybind(isClient, id, data))
                .register();

         */
        if (UseItemAbilityHandler.isUsing) {
            ci.cancel();
        }
    }
}
