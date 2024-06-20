package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property gives a fake channeling to Modular Projectiles
 */
public class ChannelingProperty extends ComplexBooleanProperty {
    public static final String KEY = "channeling";
    public static ChannelingProperty property;

    public ChannelingProperty() {
        super(KEY,false);
        property = this;
    }

    public static boolean hasChanneling(ItemStack itemStack) {
        return property.isTrue(itemStack);
    }
}
