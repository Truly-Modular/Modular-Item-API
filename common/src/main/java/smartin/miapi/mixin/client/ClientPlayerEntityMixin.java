package smartin.miapi.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(value = LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(
            method = "tickMovement()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
                    shift = At.Shift.AFTER
            )
    )
    void miapi$movementTick(CallbackInfo ci) {
        LocalPlayer clientPlayerEntity = (LocalPlayer) (Object) this;
        double attributeScale = clientPlayerEntity.getAttributeValue(AttributeRegistry.PLAYER_ITEM_USE_MOVEMENT_SPEED);
        if (isUsingItem()) {
            clientPlayerEntity.input.forwardImpulse *= (float) ((attributeScale + 1) * 5);
            clientPlayerEntity.input.leftImpulse *= (float) ((attributeScale + 1) * 5);
        }
    }
}
