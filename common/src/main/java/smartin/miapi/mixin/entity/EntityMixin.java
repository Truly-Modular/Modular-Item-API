package smartin.miapi.mixin.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.armor.ExhaustionProperty;
import smartin.miapi.modules.properties.armor.StepCancelingProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Set;

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

    @Inject(method = "Lnet/minecraft/world/entity/Entity;teleportTo(DDD)V", at = @At("HEAD"))
    private void miapi$stopTeleport(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity livingEntity &&
            MiapiConfig.getServerConfig().other.blockAllTeleportsEffect &&
            livingEntity.hasEffect(RegistryInventory.teleportBlockEffect)) {
            ci.cancel();
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
    private void miapi$stopTeleportWorld(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity livingEntity &&
            MiapiConfig.getServerConfig().other.blockAllTeleportsEffect &&
            livingEntity.hasEffect(RegistryInventory.teleportBlockEffect)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyArgs(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;vibrationAndSoundEffectsFromBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)Z")
    )
    private void miapi$adjustMakeStepNoiseEvent(Args args) {
        Entity entity = (Entity) (Object) (this);
        ExhaustionProperty.step(entity);
        args.set(2,StepCancelingProperty.makesStepNoise(entity, args.get(2)));
        args.set(3,StepCancelingProperty.makesStepNoise(entity, args.get(3)));
    }
}
