package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class PriorityProperty extends DoubleProperty {
    public static final String KEY = "priority";
    public static PriorityProperty property;


    public PriorityProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    public static double getFor(ItemModule module) {
        return property.getValueForModule(new ItemModule.ModuleInstance(module));
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
