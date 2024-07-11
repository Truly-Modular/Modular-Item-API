package smartin.miapi.modules.properties;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

/**
 * This Property allows to be set as fireproof, making them immune to Lava
 */
public class FireProof extends ComplexBooleanProperty implements ComponentApplyProperty {
    public static final String KEY = "fireProof";
    public static FireProof property;

    public FireProof() {
        super(KEY, false);
        property = this;
    }

    public static boolean fireProof(ItemStack stack) {
        return property.isTrue(stack);
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        if (fireProof(itemStack)) {
            itemStack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            itemStack.remove(DataComponents.FIRE_RESISTANT);
        }
    }
}
