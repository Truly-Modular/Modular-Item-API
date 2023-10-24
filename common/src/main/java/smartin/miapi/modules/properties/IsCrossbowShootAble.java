package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;


public class IsCrossbowShootAble extends ComplexBooleanProperty {
    public static final String KEY = "crossbowAmmunition";
    public static IsCrossbowShootAble property;

    public IsCrossbowShootAble() {
        super(KEY, false);
        property = this;
    }

    public static boolean canCrossbowShoot(ItemStack stack) {
        return property.isTrue(stack);
    }
}
