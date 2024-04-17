package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import smartin.miapi.registries.RegistryInventory;

public class StunResistanceStatusEffect extends StatusEffect {

    public StunResistanceStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, Color.WHITE.hexInt());
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if (entity.hasStatusEffect(RegistryInventory.stunEffect)) {
            entity.removeStatusEffect(RegistryInventory.stunEffect);
        }
        super.onApplied(entity, attributes, amplifier);
    }
}
