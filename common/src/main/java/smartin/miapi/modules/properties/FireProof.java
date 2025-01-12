package smartin.miapi.modules.properties;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

/**
 * @header Fire Proof Property
 * @path /data_types/properties/fire_proof
 * @description_start
 * The FireProof property allows items to be marked as fireproof, making them immune to Lava. When an item is set with this
 * property, it will not be damaged or affected by fire or lava.
 * This property is represented as a boolean value where `true` indicates that the item is fireproof, and `false` means it is not.
 * @description_end
 * @data fireProof: A boolean value indicating whether the item is immune to fire and lava.
 */

public class FireProof extends ComplexBooleanProperty implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("fire_proof");
    public static FireProof property;

    public FireProof() {
        super(KEY, false);
        property = this;
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        if (isTrue(itemStack)) {
            itemStack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        } else {
            itemStack.remove(DataComponents.FIRE_RESISTANT);
        }
    }
}
