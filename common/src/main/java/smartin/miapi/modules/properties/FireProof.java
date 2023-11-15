package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property allows to be set as fireproof, making them immune to Lava
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
