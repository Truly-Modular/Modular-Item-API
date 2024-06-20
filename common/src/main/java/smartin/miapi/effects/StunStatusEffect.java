package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.registries.RegistryInventory;

public class StunStatusEffect extends StatusEffect {

    public StunStatusEffect() {
        super(StatusEffectCategory.HARMFUL, Color.YELLOW.hexInt());
        addAttributeModifier(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                "a108d6a0-d51a-4378-9f3c-bf47243696d4",
                - 1 + MiapiConfig.INSTANCE.server.stunEffectCategory.attackSpeedFactor,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyUpdateEffect(entity, amplifier);
        if (entity.hasStatusEffect(RegistryInventory.stunResistanceEffect)) {
            entity.removeStatusEffectInternal(RegistryInventory.stunEffect);
            entity.removeStatusEffect(RegistryInventory.stunEffect);
        }
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity.hasStatusEffect(RegistryInventory.stunResistanceEffect)) {
            super.onApplied(entity, attributes, amplifier);
            return;
        }
        if (entity instanceof PlayerEntity) {
            StatusEffectInstance instance = new StatusEffectInstance(entity.getStatusEffect(this));
            if (instance != null) {
                MiapiConfig.INSTANCE.server.stunEffectCategory.playerEffects.stream()
                        .filter(Registries.STATUS_EFFECT::containsId)
                        .map(Registries.STATUS_EFFECT::get)
                        .forEach(statusEffect ->
                                entity.addStatusEffect(new StatusEffectInstance(statusEffect, instance.getDuration(), instance.getAmplifier())));
            }
        }
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(RegistryInventory.stunResistanceEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunResistanceLength, 0, false, false, true));
        super.onRemoved(entity, attributes, amplifier);
    }
}
