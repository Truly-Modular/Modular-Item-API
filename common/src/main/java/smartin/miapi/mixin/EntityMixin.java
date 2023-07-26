package smartin.miapi.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
    private void miapi$startRidingEvent(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MiapiEvents.START_RIDING.invoker().ride((Entity)(Object)this, entity);
        }
    }

    @Inject(method = "dismountVehicle", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;vehicle:Lnet/minecraft/entity/Entity;", ordinal = 2))
    private void miapi$stopRidingEvent(CallbackInfo ci) {
        Entity entity = (Entity)(Object) this;
        MiapiEvents.STOP_RIDING.invoker().ride(entity, entity.getVehicle());
    }
}
