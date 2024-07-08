package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * simple lifesteal property
 */
public class LeechingProperty extends DoubleProperty {
    public static final String KEY = "leeching";
    public static LeechingProperty property;


    public LeechingProperty() {
        super(KEY);
        property = this;

        MiapiEvents.LIVING_HURT_AFTER .register((event) -> {
            if (!event.livingEntity.level().isClientSide()) {
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
