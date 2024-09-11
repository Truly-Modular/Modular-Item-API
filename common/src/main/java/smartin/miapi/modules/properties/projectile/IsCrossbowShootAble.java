package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property marks an item as shootable by a crossbow, even if it is not an arrow.
 * @header Crossbow Shootable Property
 * @path /data_types/properties/projectile/crossbow_ammunition
 * @description_start
 * The Crossbow Shootable Property allows non-arrow items to be used as ammunition for crossbows. If this property is
 * set to true on an item, it can be fired from a crossbow.
 * @description_end
 * @data crossbow_ammunition: A boolean value indicating whether the item can be shot by a crossbow.
 */

public class IsCrossbowShootAble extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("crossbow_ammunition");
    public static IsCrossbowShootAble property;

    public IsCrossbowShootAble() {
        super(KEY, false);
        property = this;
    }

    public static boolean canCrossbowShoot(ItemStack stack) {
        return property.isTrue(stack);
    }
}
