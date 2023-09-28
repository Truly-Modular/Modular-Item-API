package smartin.miapi.item.modular;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.DurabilityProperty;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem{

    static int getDurability(ItemStack stack){
        if (stack.getItem() instanceof ModularItem) {
            return (int) DurabilityProperty.property.getValueSafe(stack);
        }
        return stack.getMaxDamage();
    }
}
