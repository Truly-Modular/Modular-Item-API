package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.registries.RegistryInventory;

public class StunResistanceStatusEffect extends MobEffect {

    public StunResistanceStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, Color.WHITE.hexInt());
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        if (entity.hasEffect(RegistryInventory.stunEffect)) {
            entity.removeEffect(RegistryInventory.stunEffect);
        }
        super.onEffectStarted(entity, amplifier);
    }
}
