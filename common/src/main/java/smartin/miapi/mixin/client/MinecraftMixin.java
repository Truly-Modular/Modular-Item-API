package smartin.miapi.mixin.client;


import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.ClientEvents;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {


    /*
    @Redirect(method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"
            )
    )
    private void redirectKeyUp(MultiPlayerGameMode instance, Player player) {
        if (!ClientKeybinding.isUsing) {
            instance.releaseUsingItem(player);
        }
    }

     */

    @Inject(method = "Lnet/minecraft/client/Minecraft;runTick(Z)V", at = @At("HEAD"))
    public void miapi$modelLoad(boolean renderLevel, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ClientEvents.CLIENT_TICK.invoker().register(minecraft);
    }
}
