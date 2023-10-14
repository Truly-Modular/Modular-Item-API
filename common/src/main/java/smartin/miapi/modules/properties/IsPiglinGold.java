package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class IsPiglinGold extends ComplexBooleanProperty {
    public static final String KEY = "isPiglinGold";
    public static IsPiglinGold property;

    public IsPiglinGold() {
        super(KEY, false);
        property = this;
    }

    public static boolean isPiglinGoldItem(ItemStack stack) {
        return property.isTrue(stack);
    }
}
