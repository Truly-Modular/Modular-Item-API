package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class DurabilityProperty extends SimpleDoubleProperty {
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
        return (int) Math.max(1, getValueSafeRaw(stack));
    }
}
