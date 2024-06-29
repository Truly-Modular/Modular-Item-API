package smartin.miapi.modules.properties;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property is arson.
 */
public class ImmolateProperty extends DoubleProperty {
    public static final String KEY = "immolate";
    public static ImmolateProperty property;


    //TODO:reimplement new Immolate
    public ImmolateProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
