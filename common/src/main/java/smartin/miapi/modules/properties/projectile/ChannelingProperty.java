package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property gives a fake channeling to Modular Projectiles
 */
public class ChannelingProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("channeling");
    public static ChannelingProperty property;

    public ChannelingProperty() {
        super(KEY,false);
        property = this;
    }

    public static boolean hasChanneling(ItemStack itemStack) {
        return property.isTrue(itemStack);
    }
}
