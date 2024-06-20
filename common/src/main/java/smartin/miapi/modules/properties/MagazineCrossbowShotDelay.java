package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class MagazineCrossbowShotDelay extends DoubleProperty {
    public static String KEY = "rapid_fire_crossbow_delay";
    public static MagazineCrossbowShotDelay property;

    public MagazineCrossbowShotDelay() {
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
