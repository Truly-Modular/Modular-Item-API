package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class ToolOrWeaponProperty implements ModuleProperty {
    public static final String KEY = "isWeapon";
    public static ToolOrWeaponProperty property;

    public ToolOrWeaponProperty() {
        property = this;
    }

    public static boolean isWeapon(ItemStack stack) {
        JsonElement element = ItemModule.getMergedProperty(stack, property);
        if (element != null) {
            return element.getAsBoolean();
        }
        return false;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsBoolean();
        return true;
    }
}
