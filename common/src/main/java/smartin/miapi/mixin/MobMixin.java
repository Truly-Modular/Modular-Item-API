package smartin.miapi.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.registries.RegistryInventory;

@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "serverAiStep()V", at = @At(value = "HEAD"), cancellable = true)
    private void miapi$stopMovementTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.hasEffect(RegistryInventory.stunEffect)) {
            var active = livingEntity.getEffect(RegistryInventory.stunEffect);
            if (active != null && active.endsWithin(1)) {
                livingEntity.removeEffect(RegistryInventory.stunEffect);
                livingEntity.addEffect(new MobEffectInstance(RegistryInventory.stunResistanceEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunResistanceLength), livingEntity);
            }
            if (livingEntity instanceof Player playerEntity) {
                if (!playerEntity.hasEffect(MobEffects.BLINDNESS)) {
                }
            } else {
                if(livingEntity instanceof Mob mob){
                    mob.setSpeed(0);
                }
                ci.cancel();
            }
        }
    }
}
