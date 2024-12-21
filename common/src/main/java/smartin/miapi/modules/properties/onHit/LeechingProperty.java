package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property implements a lifesteal mechanic, allowing entities to heal based on the damage they deal.
 *
 * @header Leeching Property
 * @path /data_types/properties/on_hit/leeching
 * @description_start
 * The Leeching Property enables a lifesteal effect, where a portion of the damage dealt by an entity is converted into health for that entity.
 * The amount of healing is proportional to the damage dealt and is affected by the level of the leeching property.
 * @description_end
 * @data value: The percentage of damage dealt that is converted into health. For example, a value of 10 means that 10% of the damage dealt is used to heal the attacker.
 */

public class LeechingProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("leeching");
    public static LeechingProperty property;


    public LeechingProperty() {
        super(KEY);
        property = this;

        MiapiEvents.LIVING_HURT_AFTER .register((event) -> {
            if (!event.defender.level().isClientSide()) {
                if (event.damageSource.getEntity() instanceof LivingEntity livingEntity) {
                    double totalLevel = getForItems(livingEntity.getAllSlots());
                    if (totalLevel > 0) {
                        double healAmount = event.amount * totalLevel / 100;
                        livingEntity.heal((float) healAmount);
                    }
                }
            }
            return EventResult.pass();
        });
    }
}
