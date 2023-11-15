package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property sets an armor Piece to be valid as a Piglin Gold Item so they dont attack the player
 */
public class IsPiglinGold extends ComplexBooleanProperty {
    public static final String KEY = "isPiglinGold";
    public static IsPiglinGold property;

    public IsPiglinGold() {
        super(KEY, false);
        property = this;
    }

    public static boolean isPiglinGoldItem(ItemStack stack) {
        return property.isTrue(stack);
    }
}
