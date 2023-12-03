package smartin.miapi.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.MiapiEvents;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void miapi$playerTickStart(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MiapiEvents.PLAYER_TICK_START.invoker().tick(player);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void miapi$playerTickEnd(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MiapiEvents.PLAYER_TICK_END.invoker().tick(player);
    }
}
