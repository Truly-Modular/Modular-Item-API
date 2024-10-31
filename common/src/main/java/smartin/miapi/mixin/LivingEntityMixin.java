package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.attributes.ElytraAttributes;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EquipmentSlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @ModifyReturnValue(method = "getPreferredEquipmentSlot", at = @At("RETURN"))
    private static EquipmentSlot miapi$onGetPreferredEquipmentSlot(EquipmentSlot original, ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            EquipmentSlot slot = EquipmentSlotProperty.getSlot(stack);
            if (slot != null) {
                return slot;
            }
        }
        return original;
    }

    @ModifyReturnValue(
            method = "Lnet/minecraft/entity/LivingEntity;getEquipmentChanges()Ljava/util/Map;",
            at = @At("RETURN"))
    private Map<EquipmentSlot, ItemStack> miapi$onEquipChange(Map<EquipmentSlot, ItemStack> original) {
        LivingEntity player = (LivingEntity) (Object) this;
        if (player instanceof PlayerEntity entity) {
            Map<EquipmentSlot, ItemStack> map = original;
            if (map!=null && !map.isEmpty()) {
                MiapiEvents.PLAYER_EQUIP_EVENT.invoker().equip(entity, map);
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "teleport(DDDZ)Z", at = @At("RETURN"))
    private boolean  miapi$optionalTeleportBlockEffect(boolean original, double x, double y, double z, boolean particleEffects) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (particleEffects && MiapiConfig.INSTANCE.server.other.blockAllTeleportsEffect && entity.hasStatusEffect(RegistryInventory.teleportBlockEffect)) {
           return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder miapi$addAttributes(DefaultAttributeContainer.Builder builder) {
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(attribute);
            });
            MiapiEvents.LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT.invoker().build(builder);
        }
        return builder;
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
                if (!playerEntity.hasStatusEffect(StatusEffects.BLINDNESS)) {
                }
            } else {
                ci.cancel();
            }
        }
    }
}
