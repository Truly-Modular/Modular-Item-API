package smartin.miapi.modules.properties.util;

import net.minecraft.world.item.ItemStack;

public abstract class ComplexBooleanProperty extends DoubleProperty {
    boolean defaultValue;

    protected ComplexBooleanProperty(String key, boolean defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
    }

    public boolean isTrue(ItemStack itemStack) {
        Double value = getValue(itemStack).orElse(null);
        return value != null ? value > 0 : defaultValue;
    }

    public boolean hasValue(ItemStack itemStack) {
        return isTrue(itemStack) != defaultValue;
    }
}
