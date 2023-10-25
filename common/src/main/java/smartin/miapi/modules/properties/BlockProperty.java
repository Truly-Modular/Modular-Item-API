package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property controls {@link smartin.miapi.modules.abilities.BlockAbility}
 */
public class BlockProperty extends DoubleProperty implements ModuleProperty {
    public static final String KEY = "blocking";
    public static BlockProperty property;

    public BlockProperty() {
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
