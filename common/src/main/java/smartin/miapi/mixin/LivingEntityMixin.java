package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.attributes.ElytraAttributes;
import smartin.miapi.config.MiapiConfig;
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

    @ModifyReturnValue(method = "randomTeleport", at = @At("RETURN"))
    private boolean miapi$optionalTeleportBlockEffect(boolean original, double x, double y, double z, boolean particleEffects) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (particleEffects && MiapiConfig.INSTANCE.server.other.blockAllTeleportsEffect && entity.hasEffect(RegistryInventory.teleportBlockEffect)) {
            return false;
        }
        return original;
    }

    @Inject(method = "createLivingAttributes", at = @At("TAIL"), cancellable = true)
    private static void miapi$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue();
        smartin.miapi.registries.AttributeRegistry.registerAttributes();
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
                //Miapi.LOGGER.info("added attribute to living entity" + id);
            });
            MiapiEvents.LIVING_ENTITY_ATTRIBUTE_BUILD_EVENT.invoker().build(builder);
        }
    }


    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void miapi$adjustElytraSpeed(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ElytraAttributes.movementUpdate(livingEntity);
        MiapiEvents.LIVING_ENTITY_TICK_END.invoker().tick(livingEntity);
    }

    @Inject(method = "baseTick", at = @At(value = "HEAD"), cancellable = true)
    private void miapi$stopMovementTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.hasEffect(RegistryInventory.stunEffect)) {
            if (livingEntity instanceof Player playerEntity) {
                if (!playerEntity.hasEffect(MobEffects.BLINDNESS)) {
                }
            } else {
                ci.cancel();
            }
        }
    }
}
