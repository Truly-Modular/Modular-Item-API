package smartin.miapi.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import smartin.miapi.registries.MiapiRegistry;

/**
 * lightweight implementation of a new damage system to allow different damage types to have individual invulnerable timer,
 * in effect bypassing the base one
 */
public class EntityDamageSystem {
    public static MiapiRegistry<AdditionalDamageEffect> REGISTRY = MiapiRegistry.getInstance(AdditionalDamageEffect.class);

    /**
     * this is called by the mixins.
     * you should not call this directly
     */
    @ApiStatus.Internal
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

    /**
     * implement and register this under {@link EntityDamageSystem#REGISTRY}.
     * each registered effect has a individual invulnerability timer.
     */
    public interface AdditionalDamageEffect {
        void apply(LivingEntity defender, DamageSource originalSource, float originalAmount, boolean didDamage);
    }
}