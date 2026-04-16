package smartin.miapi.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.ElytraAttributes;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.DamageProcessingEntity;
import smartin.miapi.entity.EntityDamageSystem;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(
            method = "collectEquipmentChanges",
            at = @At("RETURN"))
    private void miapi$enEquipChange(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        LivingEntity player = (LivingEntity) (Object) this;
        if (player instanceof Player entity) {
            Map<EquipmentSlot, ItemStack> map = cir.getReturnValue();
            if (map != null && !map.isEmpty()) {
                MiapiEvents.PLAYER_EQUIP_EVENT.invoker().equip(entity, map);
            }
        }
    }

    @Inject(method = "randomTeleport", at = @At("HEAD"))
    private void miapi$optionalTeleportBlockEffect(double x, double y, double z, boolean broadcastTeleport, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (broadcastTeleport && MiapiConfig.getServerConfig().other.blockAllTeleportsEffect && entity.hasEffect(RegistryInventory.teleportBlockEffect)) {
            cir.setReturnValue(false);
        }
    }

    @WrapMethod(
            method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
    )
    private boolean miapi$hurtWrapper(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity defender = (LivingEntity) (Object) this;
        boolean originalCall = false;
        if (defender instanceof DamageProcessingEntity damageProcessingEntity &&
            !damageProcessingEntity.miapi$isDamageProcessing()) {
            originalCall = true;
            damageProcessingEntity.miapi$setDamageProcessing(true);
        }
        boolean doesDamage = original.call(source, amount);
        if (defender instanceof DamageProcessingEntity damageProcessingEntity &&
            originalCall) {
            if(doesDamage){
                EntityDamageSystem.applyPostDamage(defender, source, amount, doesDamage);
            }
            damageProcessingEntity.miapi$setDamageProcessing(false);
        }
        return doesDamage;
    }

    @Inject(
            method = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            , at = @At(value = "TAIL")
    )
    private void miapi$hurtWrapper(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity defender = (LivingEntity) (Object) this;
        if (
                defender instanceof DamageProcessingEntity damageProcessingEntity &&
                !damageProcessingEntity.miapi$isDamageProcessing()) {
            //EntityDamageSystem.applyPostDamage(defender, source, amount, cir.getReturnValue());
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void miapi$adjustElytraSpeed(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ElytraAttributes.movementUpdate(livingEntity);
        MiapiEvents.LIVING_ENTITY_TICK_END.invoker().tick(livingEntity);
    }
}
