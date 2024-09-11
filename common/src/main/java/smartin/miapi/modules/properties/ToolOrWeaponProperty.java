package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 * @header Tool or Weapon Property
 * @path /data_types/properties/tool_or_weapon
 * @description_start
 * The ToolOrWeaponProperty designates whether an item is classified as a tool or a weapon. This classification
 * primarily affects how the item's durability is calculated and managed.
 * @description_end
 * @data is_weapon: A boolean value indicating if the item is considered a weapon.
 */

public class ToolOrWeaponProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("is_weapon");
    public static ToolOrWeaponProperty property;

    public ToolOrWeaponProperty() {
        super(KEY, false);
        property = this;
    }

    public static boolean isWeapon(ItemStack stack) {
        return property.isTrue(stack);
    }
}
