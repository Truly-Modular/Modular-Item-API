package smartin.miapi.modules.properties.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * this property sets an item as shootable by crossbow even if its not an arrow
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
