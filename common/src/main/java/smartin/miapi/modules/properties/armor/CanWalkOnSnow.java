package smartin.miapi.modules.properties.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property determins if boots can walk on powdered snow
 * @header Snow Walking Property
 * @description_start
 * This Property allows the player to walk over powdered snow
 * @desciption_end
 * @path /data_types/properties/armor/can_walk_on_snow
 * @data can_walk_on_snow:a Boolean Resolvable
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
