package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import smartin.miapi.registries.RegistryInventory;

public class StunResistanceStatusEffect extends MobEffect {

    public StunResistanceStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, Color.WHITE.hexInt());
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.hasEffect(RegistryInventory.stunEffect)) {
            entity.removeEffect(RegistryInventory.stunEffect);
        }
        super.onEffectStarted(entity, attributes, amplifier);
    }
}
