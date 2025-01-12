package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.registries.RegistryInventory;

public class StunStatusEffect extends MobEffect {

    public StunStatusEffect() {
        super(MobEffectCategory.HARMFUL, Color.YELLOW.hexInt());
        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                Miapi.id("stun_status_attackspeed_reduction"),
                -1 + MiapiConfig.INSTANCE.server.stunEffectCategory.attackSpeedFactor,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }


    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        var active = livingEntity.getEffect(RegistryInventory.stunEffect);
        if (active != null && active.endsWithin(1)) {
            livingEntity.removeEffect(RegistryInventory.stunEffect);
            livingEntity.addEffect(new MobEffectInstance(RegistryInventory.stunResistanceEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunResistanceLength), livingEntity);
        }
        if (livingEntity.hasEffect(RegistryInventory.stunResistanceEffect)) {
            livingEntity.removeEffect(RegistryInventory.stunEffect);
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            int timer = player.getEffect(RegistryInventory.stunEffect).getDuration();
            MiapiConfig.INSTANCE.server.stunEffectCategory.playerEffects.forEach(id -> {
                var attribute = player.registryAccess().registry(Registries.MOB_EFFECT).get().get(id);
                if (attribute != null) {
                    var holder = player.registryAccess().registry(Registries.MOB_EFFECT).get().wrapAsHolder(attribute);
                    player.addEffect(new MobEffectInstance(holder, timer), player);
                }
            });
        }
        super.onEffectStarted(livingEntity, amplifier);
    }

}
