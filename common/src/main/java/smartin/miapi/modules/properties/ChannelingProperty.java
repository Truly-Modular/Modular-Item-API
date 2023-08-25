package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property gives a projectile ender pearl behaviour
 */
public class ChannelingProperty implements ModuleProperty {
    public static final String KEY = "channeling";
    public static ChannelingProperty property;

    public ChannelingProperty() {
        property = this;
    }

    public static boolean hasChanneling(ItemStack itemStack) {
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
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
