package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.registries.RegistryInventory;

/**
 * This Property applies the Cryo effect on Arrowhit(only on arrowhit)
 */
public class CryoProperty extends DoubleProperty {
    public static final String KEY = "cryo";
    public static CryoProperty property;

    public CryoProperty() {
        super(KEY);
        property = this;
        /*PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> {
            double strength = getForItems(player.getHandItems());
            if (strength > 0 && target instanceof LivingEntity living) {
                int potionStrength = (int) Math.ceil(strength / 3);
                int potionLength = (int) (strength * 20 + 40);
                StatusEffectInstance instance = new StatusEffectInstance(RegistryInventory.cryoStatusEffect, potionLength, potionStrength);
                living.addStatusEffect(instance, player);
            }
            return EventResult.pass();
        });*/
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register((modularProjectileEntityHitEvent) -> {
            if (modularProjectileEntityHitEvent.entityHitResult.getEntity() instanceof LivingEntity target && target.level() instanceof ServerLevel) {
                double strength = getValue(modularProjectileEntityHitEvent.projectile.getPickupItem()).orElse(0.0);

                if (strength > 0) {
                    int potionStrength = (int) Math.ceil(strength / 3);
                    int potionLength = (int) (strength * 20 + 40);
                    MobEffectInstance instance = new MobEffectInstance(RegistryInventory.cryoStatusEffect, potionLength, potionStrength);
                    target.addEffect(instance);
                }
            }
            return EventResult.pass();
        });
    }
}
