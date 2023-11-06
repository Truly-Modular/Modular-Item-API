package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class PillagesGuard extends DoubleProperty {
    public static final String KEY = "pillagerGuard";
    public static PillagesGuard property;


    public PillagesGuard() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity living) {
                if (IllagerBane.isIllagerType(living)) {
                    double level = this.getForItems(livingHurtEvent.livingEntity.getArmorItems());
                    Miapi.DEBUG_LOGGER.warn("guard_damage " + livingHurtEvent.amount + " " + valueRemap(level));
                    Miapi.DEBUG_LOGGER.warn(String.valueOf(livingHurtEvent.livingEntity.getAttributes().getValue(EntityAttributes.GENERIC_ARMOR))+ " "+livingHurtEvent.livingEntity.getWorld().isClient());
                    livingHurtEvent.amount *= (float) valueRemap(level);
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
