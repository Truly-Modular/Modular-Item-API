package smartin.miapi.modules.properties.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property sets an armor Piece to be valid as a Piglin Gold Item so they don't attack the player
 * @header IsPiglinGold Property
 * @description_start
 * This Property allows to set armor pieces as Piglin pacifying like gold is
 * @desciption_end
 * @path /data_types/properties/armor/is_piglin_gold
 * @data is_piglin_gold:a Boolean Resolvable
 */
public class IsPiglinGold extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("is_piglin_gold");
    public static IsPiglinGold property;

    public IsPiglinGold() {
        super(KEY, false);
        property = this;
    }

    public static boolean isPiglinGoldItem(ItemStack stack) {
        return property.isTrue(stack);
    }
}
