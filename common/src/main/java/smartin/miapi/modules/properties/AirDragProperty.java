package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This Property manages speedloss of {@link smartin.miapi.entity.ItemProjectileEntity} of Modular Items
 */
public class AirDragProperty extends DoubleProperty {
    public static final String KEY = "air_drag";
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
