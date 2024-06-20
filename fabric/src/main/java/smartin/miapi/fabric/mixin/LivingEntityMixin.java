package smartin.miapi.fabric.mixin;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.LivingEntityAccessor;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    private float storedValue;
    private DamageSource storedDamageSource;
    private MiapiEvents.LivingHurtEvent lastEvent;

    @Inject(method = "damage", at = @At(value = "HEAD"))
    private void miapi$damageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        if (source.getAttacker() instanceof PlayerEntity entity) {
            livingHurtEvent.isCritical = hasCrited(entity, (LivingEntity) (Object) this);
        }
        if (source.getAttacker() instanceof ArrowEntity arrowEntity) {
            //livingHurtEvent.isCritical = arrowEntity.isCritical();
        }
        EventResult result = MiapiEvents.LIVING_HURT.invoker().hurt(livingHurtEvent);
        if (result.interruptsFurtherEvaluation()) {
            cir.setReturnValue(false);
        }
        lastEvent = livingHurtEvent;
        storedValue = livingHurtEvent.amount;
        storedDamageSource = livingHurtEvent.damageSource;
    }

    @ModifyVariable(
            method = "modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F",
            at = @At(value = "HEAD"),
            ordinal = 0)
    private float miapi$modiyAppliedDamageEvent(float amount) {
        //float damage = Math.max(0, currentShieldingArmor);
        //amount -= damage;
        //currentShieldingArmor = currentShieldingArmor - Math.min(amount, damage);
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, storedDamageSource, amount);
        if(storedDamageSource!=null){
            if (storedDamageSource.getAttacker() instanceof PlayerEntity entity) {
                livingHurtEvent.isCritical = hasCrited(entity, (LivingEntity) (Object) this);
            }
            if (storedDamageSource.getAttacker() instanceof ArrowEntity arrowEntity) {
                //livingHurtEvent.isCritical = arrowEntity.isCritical();
            }
        }
        MiapiEvents.LIVING_HURT_AFTER_ARMOR.invoker().hurt(livingHurtEvent);
        return livingHurtEvent.amount;
    }

    @Unique
    private boolean hasCrited(PlayerEntity attacker, LivingEntity defender) {
        return Boolean.TRUE.equals(AttributeRegistry.hasCrittedLast.putIfAbsent(attacker, false));
    }

    @Inject(method = "damage", at = @At(value = "TAIL"))
    private void miapi$damageEventAfter(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        float lastDamageTaken = ((LivingEntityAccessor) livingEntity).getLastDamageTaken();
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, source, lastDamageTaken);
        livingHurtEvent.isCritical = lastEvent.isCritical;
        MiapiEvents.LIVING_HURT_AFTER.invoker().hurt(livingHurtEvent);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), ordinal = 0)
    private float miapi$damageEventValue(float value) {
        return storedValue;
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), ordinal = 0)
    private DamageSource miapi$damageEventSource(DamageSource value) {
        return storedDamageSource;
    }
}
