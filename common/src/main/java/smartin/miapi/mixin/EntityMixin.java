package smartin.miapi.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.armor.ExhaustionProperty;
import smartin.miapi.modules.properties.armor.StepCancelingProperty;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;)Z", at = @At("TAIL"))
    private void miapi$startRidingEvent(Entity vehicle, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MiapiEvents.START_RIDING.invoker().ride((Entity) (Object) this, vehicle);
        }
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    private void miapi$stopRidingEvent(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        MiapiEvents.STOP_RIDING.invoker().ride(entity, entity.getVehicle());
    }

    @ModifyVariable(
            method = "vibrationAndSoundEffectsFromBlock",
            at = @At(value = "RETURN"),
            ordinal = 1)
    private boolean miapi$adjustMakeStepNoiseEvent(boolean value) {
        Entity entity = (Entity) (Object) (this);
        ExhaustionProperty.step(entity);
        return StepCancelingProperty.makesStepNoise(entity, value);
    }

    @ModifyVariable(
            method = "vibrationAndSoundEffectsFromBlock",
            at = @At(value = "RETURN"),
            ordinal = 0)
    private boolean miapi$adjustMakeStepNoiseSound(boolean value) {
        Entity entity = (Entity) (Object) (this);
        return StepCancelingProperty.makesStepNoise(entity, value);
    }
}
