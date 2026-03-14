package smartin.miapi.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.entity.DamageProcessingEntity;

@Mixin(LivingEntity.class)
public class LivingEntityDamageFlagMixin implements DamageProcessingEntity {

    @Unique
    private boolean miapi$damageProcessing = false;

    @Override
    public boolean miapi$isDamageProcessing() {
        return miapi$damageProcessing;
    }

    @Override
    public void miapi$setDamageProcessing(boolean processing) {
        miapi$damageProcessing = processing;
    }
}