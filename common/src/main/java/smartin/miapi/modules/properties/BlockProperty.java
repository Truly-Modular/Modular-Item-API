package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property controls {@link smartin.miapi.modules.abilities.BlockAbility}
 */
public class BlockProperty extends DoubleProperty{
    public static final String KEY = "blocking";
    public static BlockProperty property;

    public BlockProperty() {
        super(KEY);
        property = this;
    }

    public double getValueSafe(ItemStack stack) {
        return getValue(stack).orElse(0.0);
    }
}
