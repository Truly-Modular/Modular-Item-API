package smartin.miapi.modules.properties.util;

import net.minecraft.world.item.ItemStack;

public abstract class ComplexBooleanProperty extends DoubleProperty {
    boolean defaultValue;
    //TODO:add a gui to this and all its inheritors

    protected ComplexBooleanProperty(String key, boolean defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
    }

    public boolean isTrue(ItemStack itemStack) {
        Double value = getValueRaw(itemStack);
        return value != null ? value > 0 : defaultValue;
    }

    public boolean hasValue(ItemStack itemStack) {
        return isTrue(itemStack) != defaultValue;
    }


    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
