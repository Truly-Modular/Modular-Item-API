package smartin.miapi.mixin;

import dev.architectury.event.EventResult;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.attributes.ElytraAttributes;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EquipmentSlotProperty;
import smartin.miapi.registries.RegistryInventory;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private static void miapi$onGetPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (stack.getItem() instanceof ModularItem) {
            EquipmentSlot slot = EquipmentSlotProperty.getSlot(stack);
            if (slot != null) {
                cir.setReturnValue(slot);
            }
        }
    }

    @Inject(method = "teleport(DDDZ)Z", at = @At("HEAD"), cancellable = true)
    private void miapi$optionalTeleportBlockEffect(double x, double y, double z, boolean particleEffects, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (particleEffects &&  MiapiConfig.INSTANCE.server.other.blockAllTeleportsEffect && entity.hasStatusEffect(RegistryInventory.teleportBlockEffect)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "createLivingAttributes", at = @At("TAIL"), cancellable = true)
    private static void miapi$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(attribute);
            });
            MiapiEvents.LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT.invoker().build(builder);
        }
    }


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

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void miapi$adjustElytraSpeed(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ElytraAttributes.movementUpdate(livingEntity);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), ordinal = 0)
    private DamageSource miapi$damageEventSource(DamageSource value) {
        return storedDamageSource;
    }
}
