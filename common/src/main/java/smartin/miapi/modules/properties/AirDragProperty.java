package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class AirDragProperty extends SimpleDoubleProperty {
    public static String KEY = "air_drag";
    public static AirDragProperty property;

    public AirDragProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return this.getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        Double value = getValueRaw(stack);
        return value == null ? 1 : value;
    }
}
