package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class DurabilityProperty extends DoubleProperty {
    public static final String KEY = "durability";
    public static DurabilityProperty property;

    public DurabilityProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public Double getValue(ItemStack stack) {
        Double value = getValueRaw(stack);
        if (value != null) {
            return value;
        }
        return null;
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return (int) Math.max(1, Math.round(getValueSafeRaw(stack)));
    }
}
