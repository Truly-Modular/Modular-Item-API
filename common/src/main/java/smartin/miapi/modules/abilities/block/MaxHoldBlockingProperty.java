package smartin.miapi.modules.abilities.block;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property controls {@link smartin.miapi.modules.abilities.BlockAbility}
 */
public class MaxHoldBlockingProperty extends DoubleProperty implements ModuleProperty {
    public static final String KEY = "max_hold_blocking";
    public static MaxHoldBlockingProperty property;

    public MaxHoldBlockingProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return this.getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return this.getValueSafeRaw(stack);
    }
}
