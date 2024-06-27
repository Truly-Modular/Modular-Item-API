package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.damage_boosts.IllagerBane;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * decreases damage from raid type mobs
 */
public class PillagesGuard extends DoubleProperty {
    public static final String KEY = "pillagerGuard";
    public static PillagesGuard property;


    public PillagesGuard() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (livingHurtEvent.damageSource.getEntity() instanceof LivingEntity living) {
                if (IllagerBane.isIllagerType(living) && !living.level().isClientSide()) {
                    double level = 1;
                    for (ItemStack itemStack : livingHurtEvent.livingEntity.getArmorSlots()) {
                        level -= (1 - valueRemap(getValueSafe(itemStack)));
                    }
                    livingHurtEvent.amount *= (float) level;
                }
            }
            return EventResult.pass();
        });
    }

    public static double valueRemap(double x) {
        return 1 - (2 / (1 + Math.exp(-x / 10)) - 1);
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
