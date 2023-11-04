package smartin.miapi.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(value = ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tickMovement()V", at = @At("HEAD"))
    void miapi$renderArmorInject(CallbackInfo ci) {
        ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) (Object) this;
        double attributeScale = clientPlayerEntity.getAttributeValue(AttributeRegistry.PLAYER_ITEM_USE_MOVEMENT_SPEED);
        clientPlayerEntity.input.movementForward *= (float) (attributeScale * 5);
        clientPlayerEntity.input.movementSideways *= (float) (attributeScale * 5);
    }
}
