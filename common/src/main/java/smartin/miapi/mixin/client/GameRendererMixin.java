package smartin.miapi.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.ClientEvents;

@Mixin(GameRenderer.class)
public class GameRendererMixin {


    @Inject(
            method = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet / minecraft / client / DeltaTracker;Z)V",
            at = @At("HEAD")
    )
    private void miapi$customItemRenderingEntityGetter(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ClientEvents.CLIENT_RENDER_TICK.invoker().register(Minecraft.getInstance());
    }
}
