package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This Property manages speedloss of {@link smartin.miapi.entity.ItemProjectileEntity} of Modular Items
 */
public class AirDragProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("air_drag");
    public static AirDragProperty property;

    public AirDragProperty() {
        super(KEY);
        property = this;
    }

    public double getValueSafe(ItemStack stack) {
        return getValue(stack).orElse(0.0);
    }
}
