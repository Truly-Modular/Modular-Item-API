package smartin.miapi.modules.properties.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property determins if boots can walk on powdered snow
 */
public class CanWalkOnSnow extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("can_walk_on_snow");
    public static CanWalkOnSnow property;

    public CanWalkOnSnow() {
        super(KEY, false);
        property = this;
    }

    public static boolean canSnowWalk(ItemStack stack) {
        return property.isTrue(stack);
    }
}
