package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property applies a fake channeling effect to Modular Projectiles.
 * @header Channeling Property
 * @path /data_types/properties/projectile/channeling
 * @description_start
 * The Channeling Property grants a modular projectile the channeling effect.
 * This means it will strike lightning on a target when its rainstorming.
 * @description_end
 * @data channeling: Indicates whether the projectile has the channeling effect applied.
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
