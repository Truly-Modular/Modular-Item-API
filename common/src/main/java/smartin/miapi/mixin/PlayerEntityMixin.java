package smartin.miapi.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(
            method = "attack",
            at = @At("HEAD"))
    private void miapi$captureCritHEAD(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient()) {
            if (target instanceof LivingEntity livingEntity) {
                AttributeRegistry.hasCrittedLast.put(player, hasCritted(player, livingEntity));
            } else {
                AttributeRegistry.hasCrittedLast.put(player, false);
            }
        }
    }

    public boolean hasCritted(PlayerEntity attacker, LivingEntity defender) {
        float h = attacker.getAttackCooldownProgress(0.5F);
        boolean bl = h > 0.9F;
        return bl && attacker.fallDistance > 0.0F && !attacker.isOnGround() && !attacker.isClimbing() && !attacker.isTouchingWater() && !attacker.hasStatusEffect(StatusEffects.BLINDNESS) && !attacker.hasVehicle() && defender instanceof LivingEntity;
    }

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
