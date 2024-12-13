package smartin.miapi.fabric.mixin;

import dev.architectury.event.EventResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
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

    @Inject(method = "hurt", at = @At(value = "HEAD"))
    private void miapi$damageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        if (source.getEntity() instanceof Player entity) {
            livingHurtEvent.isCritical = hasCrited(entity, (LivingEntity) (Object) this);
        }
        if (source.getEntity() instanceof Arrow arrowEntity) {
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

    @Inject(method = "createLivingAttributes", at = @At("TAIL"), cancellable = true)
    private static void miapi$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue();
        smartin.miapi.registries.AttributeRegistry.registerAttributes();
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute.value()));
                //Miapi.LOGGER.info("added attribute to living entity" + id);
            });
            MiapiEvents.LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT.invoker().build(builder);
        }
    }

    @ModifyVariable(
            method = "getDamageAfterArmorAbsorb",
            at = @At(value = "HEAD"),
            ordinal = 0)
    private float miapi$modiyAppliedDamageEvent(float amount) {
        //float damage = Math.max(0, currentShieldingArmor);
        //amount -= damage;
        //currentShieldingArmor = currentShieldingArmor - Math.min(amount, damage);
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, storedDamageSource, amount);
        if(storedDamageSource!=null){
            if (storedDamageSource.getEntity() instanceof Player entity) {
                livingHurtEvent.isCritical = hasCrited(entity, (LivingEntity) (Object) this);
            }
            if (storedDamageSource.getEntity() instanceof Arrow arrowEntity) {
                //livingHurtEvent.isCritical = arrowEntity.isCritical();
            }
        }
        MiapiEvents.LIVING_HURT_AFTER_ARMOR.invoker().hurt(livingHurtEvent);
        return livingHurtEvent.amount;
    }

    @Unique
    private boolean hasCrited(Player attacker, LivingEntity defender) {
        return Boolean.TRUE.equals(AttributeRegistry.hasCrittedLast.putIfAbsent(attacker, false));
    }

    @Inject(method = "hurt", at = @At(value = "TAIL"))
    private void miapi$damageEventAfter(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        float lastDamageTaken = ((LivingEntityAccessor) livingEntity).getLastDamageTaken();
        MiapiEvents.LivingHurtEvent livingHurtEvent = new MiapiEvents.LivingHurtEvent((LivingEntity) (Object) this, source, lastDamageTaken);
        livingHurtEvent.isCritical = lastEvent.isCritical;
        MiapiEvents.LIVING_HURT_AFTER.invoker().hurt(livingHurtEvent);
    }

    @ModifyVariable(method = "hurt", at = @At(value = "HEAD"), ordinal = 0)
    private float miapi$damageEventValue(float value) {
        return storedValue;
    }

    @ModifyVariable(method = "hurt", at = @At(value = "HEAD"), ordinal = 0)
    private DamageSource miapi$damageEventSource(DamageSource value) {
        return storedDamageSource;
    }
}
