package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.BooleanProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.net.http.HttpResponse;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class ToolOrWeaponProperty extends BooleanProperty {
    public static final String KEY = "isWeapon";
    public static ToolOrWeaponProperty property;

    public ToolOrWeaponProperty() {
        super(KEY);
        property = this;
    }

    public static boolean isWeapon(ItemStack stack) {
        return isTrue(stack);
    }
}
