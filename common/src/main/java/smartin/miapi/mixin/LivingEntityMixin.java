package smartin.miapi.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    //@Unique
    //public float currentShieldingArmor = 0;

    @Shadow
    @Final
    public LimbAnimator limbAnimator;

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
        if (particleEffects && MiapiConfig.INSTANCE.server.other.blockAllTeleportsEffect && entity.hasStatusEffect(RegistryInventory.teleportBlockEffect)) {
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


    @Inject(method = "Lnet/minecraft/entity/LivingEntity;tick()V", at = @At("TAIL"), cancellable = true)
    private void miapi$tickShieldingArmor(CallbackInfo ci) {
        //TODO:shielding armor
        /*
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.getLastAttackedTime() + 20 * 30 < livingEntity.age && livingEntity.age % 40 == 3) {
            if (livingEntity.getAttributes().hasAttribute(AttributeRegistry.SHIELDING_ARMOR)) {
                double maxArmor = livingEntity.getAttributeValue(AttributeRegistry.SHIELDING_ARMOR);
                currentShieldingArmor = (float) Math.min(maxArmor, currentShieldingArmor + 1);
                if (livingEntity instanceof PlayerEntity) {
                    //Miapi.LOGGER.info("shielding armor grow " + maxArmor + " " + currentShieldingArmor);
                }
            }
        }
        if (livingEntity instanceof PlayerEntity) {
            //Miapi.LOGGER.info("current SHIELDING ARMOR " + currentShieldingArmor);
        }
         */
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void miapi$adjustElytraSpeed(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ElytraAttributes.movementUpdate(livingEntity);
        MiapiEvents.LIVING_ENTITY_TICK_END.invoker().tick(livingEntity);
    }

    @Inject(method = "tickMovement()V", at = @At(value = "HEAD"), cancellable = true)
    private void miapi$stopMovementTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.hasStatusEffect(RegistryInventory.stunEffect)) {
            if (livingEntity instanceof PlayerEntity playerEntity) {
                if(!playerEntity.hasStatusEffect(StatusEffects.BLINDNESS)){
                }
            } else {
                ci.cancel();
            }
        }
    }
}
