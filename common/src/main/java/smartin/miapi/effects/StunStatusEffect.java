package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.registries.RegistryInventory;

public class StunStatusEffect extends MobEffect {

    public StunStatusEffect() {
        super(MobEffectCategory.HARMFUL, Color.YELLOW.hexInt());
        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "a108d6a0-d51a-4378-9f3c-bf47243696d4",
                - 1 + MiapiConfig.INSTANCE.server.stunEffectCategory.attackSpeedFactor,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.hasEffect(RegistryInventory.stunResistanceEffect)) {
            entity.removeEffectNoUpdate(RegistryInventory.stunEffect);
            entity.removeEffect(RegistryInventory.stunEffect);
        }
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.hasEffect(RegistryInventory.stunResistanceEffect)) {
            super.onEffectStarted(entity, attributes, amplifier);
            return;
        }
        if (entity instanceof Player) {
            MobEffectInstance instance = new MobEffectInstance(entity.getEffect(this));
            if (instance != null) {
                MiapiConfig.INSTANCE.server.stunEffectCategory.playerEffects.stream()
                        .filter(BuiltInRegistries.MOB_EFFECT::containsKey)
                        .map(BuiltInRegistries.MOB_EFFECT::get)
                        .forEach(statusEffect ->
                                entity.addEffect(new MobEffectInstance(statusEffect, instance.getDuration(), instance.getAmplifier())));
            }
        }
        super.onEffectStarted(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeMap attributes, int amplifier) {
        entity.addEffect(new MobEffectInstance(RegistryInventory.stunResistanceEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunResistanceLength, 0, false, false, true));
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
