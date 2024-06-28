package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;

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
        Miapi.LOGGER.info("apply effect tick this tick - stun status");
        return super.shouldApplyEffectTickThisTick(duration, amplifier);
    }

    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        Miapi.LOGGER.info("apply effect tick - stun status");
        return super.applyEffectTick(livingEntity, amplifier);
    }

    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        Miapi.LOGGER.info("apply effect started - stun status");
        super.onEffectStarted(livingEntity, amplifier);
    }

}
