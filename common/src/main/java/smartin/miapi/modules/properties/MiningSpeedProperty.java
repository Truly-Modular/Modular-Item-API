package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class MiningSpeedProperty extends DoubleProperty {
    public static MiningSpeedProperty property;
    public static String KEY = "mining_speed_modifier";

    public MiningSpeedProperty() {
        super(KEY);
        property = this;
        this.baseValue = 1;
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
