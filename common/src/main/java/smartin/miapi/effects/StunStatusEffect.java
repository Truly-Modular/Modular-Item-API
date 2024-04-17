package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import smartin.miapi.registries.RegistryInventory;

public class StunStatusEffect extends StatusEffect {

    public StunStatusEffect() {
        super(StatusEffectCategory.HARMFUL, Color.YELLOW.hexInt());
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
                StatusEffectInstance blindness = new StatusEffectInstance(StatusEffects.BLINDNESS, instance.getDuration(), instance.getAmplifier());
                StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, instance.getDuration(), instance.getAmplifier());

                entity.addStatusEffect(blindness);
                entity.addStatusEffect(slowness);
            }
        }
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(RegistryInventory.stunResistanceEffect, 20 * 30, 0, false, false, true));
        super.onRemoved(entity, attributes, amplifier);
    }
}
