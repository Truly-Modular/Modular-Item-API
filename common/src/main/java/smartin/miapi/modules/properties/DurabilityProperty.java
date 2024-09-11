package smartin.miapi.modules.properties;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Durability Property
 * @path /data_types/properties/durability
 * @description_start
 * The DurabilityProperty controls the maximum durability of an item. The durability is specified as a Double Resolvable value,
 * which determines the item's maximum damage capacity. This value is applied to the item stack, influencing how much wear and
 * tear the item can withstand before breaking.
 * @description_end
 * @data durability: A Double value representing the maximum durability of the item.
 */
public class DurabilityProperty extends DoubleProperty implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("durability");
    public static DurabilityProperty property;

    public DurabilityProperty() {
        super(KEY);
        property = this;
        allowVisualOnly = true;
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        itemStack.set(DataComponents.MAX_DAMAGE,getValue(itemStack).orElse(50.0).intValue());
    }
}
