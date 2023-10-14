package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property allows to designate the Item as Tool or Weapon,
 * mainly affecting durability calculations
 */
public class FireProof extends ComplexBooleanProperty {
    public static final String KEY = "fireProof";
    public static FireProof property;

    public FireProof() {
        super(KEY, false);
        property = this;
    }

    public static boolean fireProof(ItemStack stack) {
        return property.isTrue(stack);
    }
}
