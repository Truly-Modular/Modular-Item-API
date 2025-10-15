package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.onHit.entity.EntityArmorStrength;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Immolate Property
 * @path /data_types/properties/on_hit/immolate
 * @description_start Increases Damage to target on fire.
 * Decreases Damage while self on fire.
 * @description_end
 * @data value:
 */

public class ImmolateProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("immolate");
    public static ImmolateProperty property;

    public ImmolateProperty() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register(event -> {
            if (!event.defender.level().isClientSide()) {
                double immolate = getForItems(event.defender.getAllSlots());
                if (immolate > 0) {
                    event.amount *= (1 - (float) EntityArmorStrength.valueRemap(immolate));
                }

            }
            return EventResult.pass();
        });
        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            if (target.isOnFire()) {
                if (damageSource.getEntity() != null && damageSource.getEntity().isOnFire() && damageSource.getEntity() instanceof LivingEntity livingEntity) {
                    double immolate = getForItems(livingEntity.getAllSlots());
                    if (immolate > 0) {
                        bonusDamage.add(baseDamage * immolate / 50.0);
                    }
                } else {
                    getData(itemStack).ifPresent(immolate -> {
                        bonusDamage.add(baseDamage * immolate.getValue() / 50.0);
                    });
                }
            }
            if (damageSource.getEntity() != null && damageSource.getEntity().isOnFire() && damageSource.getEntity() instanceof LivingEntity livingEntity) {
                double immolate = getForItems(livingEntity.getAllSlots());
                if (immolate > 0) {
                    bonusDamage.add(baseDamage * immolate / 50.0);
                }
            }
            return EventResult.pass();
        });
    }
}
