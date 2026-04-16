package smartin.miapi.entity;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import smartin.miapi.registries.MiapiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * lightweight implementation of a new damage system to allow different damage types to have individual invulnerable timer,
 * in effect bypassing the base one
 */
public class EntityDamageSystem {
    public static MiapiRegistry<AdditionalDamageEffect> REGISTRY = MiapiRegistry.getInstance(AdditionalDamageEffect.class);
    public static PrioritizedEvent<DamageEvent> DAMAGE_EVENT = PrioritizedEvent.createEventResult();

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
        float attackStrength;
        if (originalSource.getEntity() instanceof Player attacker) {
            attackStrength = ((PlayerLastAttackStrengthAccessor) attacker).getLast();
        } else {
            attackStrength = 1.0f;
        }
        int baseHurtTime = defender.hurtTime;
        int baseHurtDuration = defender.hurtDuration;
        int invulnerabilityTime = defender.invulnerableTime;
        REGISTRY.getFlatMap().forEach((id, post) -> {
            facet.get(id).apply(defender);
            post.apply(defender, originalSource, originalAmount, attackStrength);
            facet.get(id).read(defender);
        });
        DAMAGE_EVENT.invoker().apply(defender, originalSource, originalAmount, attackStrength, (id, effect) -> {
            facet.get(id).apply(defender);
            effect.run();
            facet.get(id).read(defender);
        });
        defender.hurtTime = baseHurtTime;
        defender.hurtDuration = baseHurtDuration;
        defender.invulnerableTime = invulnerabilityTime;
    }

    public static ItemStack getMainCausingStack(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof Projectile projectile) {
            ItemStack bow = ((ProjectileWithBow) projectile).getBowItem();
            if (bow != null && !bow.isEmpty()) {
                return bow;
            }
            if (projectile instanceof ItemProjectileEntity itemProjectile) {
                return itemProjectile.getProjectileItem();
            }
        } else if (damageSource.getEntity() instanceof LivingEntity attacker) {
            return attacker.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getBowItemStack(DamageSource damageSource) {
        if (damageSource.getDirectEntity() instanceof Projectile projectile) {
            ItemStack bow = ((ProjectileWithBow) projectile).getBowItem();
            if (bow != null && !bow.isEmpty()) {
                return bow;
            }
        }
        return ItemStack.EMPTY;
    }

    public static Iterable<ItemStack> getCausingItemStackAndArmorOfAttacker(DamageSource damageSource) {
        List<ItemStack> itemStacks = new ArrayList<>();
        if (damageSource.getDirectEntity() instanceof Projectile projectile) {
            ItemStack bow = ((ProjectileWithBow) projectile).getBowItem();
            if (bow != null && !bow.isEmpty()) {
                itemStacks.add(bow);
            }
            if (projectile instanceof ItemProjectileEntity itemProjectile) {
                itemStacks.add(itemProjectile.getProjectileItem());
            }
        } else {
            if (damageSource.getEntity() instanceof LivingEntity attacker) {
                itemStacks.add(attacker.getMainHandItem());
            }
        }
        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            EntityHelper.getEquipedNonHandItems(attacker).forEach(itemStacks::add);
        }
        return itemStacks;
    }

    /**
     * implement and register this under {@link EntityDamageSystem#REGISTRY}.
     * each registered effect has a individual invulnerability timer.
     */
    public interface AdditionalDamageEffect {
        void apply(LivingEntity defender, DamageSource originalSource, float originalAmount, float attackStrength);
    }

    public interface DamageEvent {
        EventResult apply(LivingEntity defender, DamageSource originalSource, float originalAmount, float attackStrength,
                          BiConsumer<ResourceLocation, Runnable> applyCustomEffect);
    }
}