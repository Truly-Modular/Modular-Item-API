package smartin.miapi.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.StepCancelingProperty;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract boolean equals(Object o);

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
    private void miapi$startRidingEvent(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MiapiEvents.START_RIDING.invoker().ride((Entity) (Object) this, entity);
        }
    }

    @Inject(method = "dismountVehicle", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;vehicle:Lnet/minecraft/entity/Entity;", ordinal = 2))
    private void miapi$stopRidingEvent(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        MiapiEvents.STOP_RIDING.invoker().ride(entity, entity.getVehicle());
    }

    @ModifyVariable(
            method = "stepOnBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;ZZLnet/minecraft/util/math/Vec3d;)Z",
            at = @At(value = "HEAD"),
            ordinal = 1)
    private boolean miapi$adjustMakeStepNoise(boolean value) {
        Entity entity = (Entity) (Object) (this);
        return StepCancelingProperty.makesStepNoise(entity, value);
    }
}
