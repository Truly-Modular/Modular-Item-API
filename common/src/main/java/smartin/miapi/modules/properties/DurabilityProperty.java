package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property manages the final durabiltiy of the item
 */
public class DurabilityProperty extends DoubleProperty {
    public static final String KEY = "durability";
    public static DurabilityProperty property;

    public DurabilityProperty() {
        super(KEY);
        property = this;
        allowVisualOnly = true;
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
