package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.BooleanProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class ToolOrWeaponProperty extends BooleanProperty {
    public static final String KEY = "isWeapon";
    public static ToolOrWeaponProperty property;

    public ToolOrWeaponProperty() {
        super(KEY, false);
        property = this;
    }

    public static boolean isWeapon(ItemStack stack) {
        return property.isTrue(stack);
    }
}
