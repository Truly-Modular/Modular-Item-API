package smartin.miapi.modules.properties.util;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.ModularAttackEvents;

public abstract class EntityDamageBoostProperty extends DoubleProperty {

    public EntityDamageBoostProperty(ResourceLocation key, isOfEntity entityPredicate) {
        super(key);
        ModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            if (!target.level().isClientSide() && target instanceof LivingEntity livingEntity) {
                if (entityPredicate.test(livingEntity)) {
                    bonusDamage.add(getValue(itemStack).orElse(0.0));
                }
            }
            return EventResult.pass();
        });
    }

    public interface isOfEntity {
        boolean test(LivingEntity living);
    }
}
