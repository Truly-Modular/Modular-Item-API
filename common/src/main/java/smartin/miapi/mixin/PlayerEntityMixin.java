package smartin.miapi.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Inject(
            method = "attack",
            at = @At("HEAD"))
    private void miapi$captureCritHEAD(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide()) {
            if (target instanceof LivingEntity livingEntity) {
                AttributeRegistry.hasCrittedLast.put(player, hasCritted(player, livingEntity));
            } else {
                AttributeRegistry.hasCrittedLast.put(player, false);
            }
        }
    }

    public boolean hasCritted(Player attacker, LivingEntity defender) {
        float h = attacker.getAttackStrengthScale(0.5F);
        boolean bl = h > 0.9F;
        return bl && attacker.fallDistance > 0.0F && !attacker.onGround() && !attacker.onClimbable() && !attacker.isInWater() && !attacker.hasEffect(MobEffects.BLINDNESS) && !attacker.isPassenger() && defender instanceof LivingEntity;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void miapi$playerTickStart(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        MiapiEvents.PLAYER_TICK_START.invoker().tick(player);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void miapi$playerTickEnd(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        MiapiEvents.PLAYER_TICK_END.invoker().tick(player);
    }
}
