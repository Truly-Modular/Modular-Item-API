package smartin.miapi.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.registries.MiapiRegistry;

public class EntityDamageSystem {
    public static MiapiRegistry<AdditionalDamageEffect> REGISTRY = MiapiRegistry.getInstance(AdditionalDamageEffect.class);

    public static void applyPostDamage(LivingEntity defender, DamageSource originalSource, float originalAmount, boolean didDamage) {
        HurtTimerFacet facet = HurtTimerFacet.KEY.get(defender);
        if (facet == null) {
            return;
        }
        int baseHurtTime = defender.hurtTime;
        int baseHurtDuration = defender.hurtDuration;
        REGISTRY.getFlatMap().forEach((id, post) -> {
            facet.get(id).apply(defender);
            post.apply(defender, originalSource, originalAmount, didDamage);
            facet.get(id).read(defender);
        });
        defender.hurtTime = baseHurtTime;
        defender.hurtDuration = baseHurtDuration;
    }

    public static class DamageSnapshot {
        public int hurtTime = 0;
        public int hurtDuration = 0;

        public void apply(LivingEntity livingEntity) {
            livingEntity.hurtTime = this.hurtTime;
            livingEntity.hurtDuration = this.hurtDuration;
        }
    }

    public interface AdditionalDamageEffect {
        void apply(LivingEntity defender, DamageSource originalSource, float originalAmount, boolean didDamage);
    }
}