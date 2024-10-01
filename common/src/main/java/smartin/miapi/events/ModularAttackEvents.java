package smartin.miapi.events;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableFloat;

/**
 * These events are only called when a modular weapon performs a melee attack
 */
public class ModularAttackEvents {
    /**
     * if interrupted cancels the attack like creative with a sword in the hand
     * This is called via {@link net.minecraft.world.item.Item#hurtEnemy(ItemStack, LivingEntity, LivingEntity)}
     */
    public static final PrioritizedEvent<HurtEnemy> HURT_ENEMY = PrioritizedEvent.createEventResult();
    /**
     * This Event should be used whenever you want to have conditional damage on modular items
     * This is called via {@link net.minecraft.world.item.Item#postHurtEnemy(ItemStack, LivingEntity, LivingEntity)}
     */
    public static final PrioritizedEvent<HurtEnemy> HURT_ENEMY_POST = PrioritizedEvent.createEventResult();
    /**
     * This Event should be used whenever you want to have conditional damage on modular items
     * This is called via {@link net.minecraft.world.item.Item#getAttackDamageBonus(Entity, float, DamageSource)}
     */
    public static final PrioritizedEvent<GetAttackDamageBonus> ATTACK_DAMAGE_BONUS = PrioritizedEvent.createEventResult();


    public interface HurtEnemy {
        EventResult hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker);
    }

    public interface GetAttackDamageBonus {
        EventResult getAttackDamageBonus(Entity target, ItemStack itemStack, float baseDamage, DamageSource damageSource, MutableFloat bonusDamage);
    }
}
