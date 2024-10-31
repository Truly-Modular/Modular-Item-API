package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class SlashingProperty extends DoubleProperty {
    public static String KEY = "slashing";
    public static SlashingProperty property;

    public SlashingProperty() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            double slashing = getValueSafe(livingHurtEvent.getCausingItemStack());
            slashing -= livingHurtEvent.livingEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            slashing -= livingHurtEvent.livingEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            if (slashing > 0) {
                livingHurtEvent.amount += (float) slashing;
            }
            return EventResult.pass();
        });
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueSafe(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
