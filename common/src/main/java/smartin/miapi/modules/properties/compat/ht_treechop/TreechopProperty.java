package smartin.miapi.modules.properties.compat.ht_treechop;

import dev.architectury.platform.Platform;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class TreechopProperty extends DoubleProperty {
    public static String KEY = "ht_treechop_swing";
    public static TreechopProperty property;

    public TreechopProperty() {
        super(KEY);
        property = this;
        if (Platform.isModLoaded("treechop")) {
            TreechopUtil.setup();
        }
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
