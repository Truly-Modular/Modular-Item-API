package smartin.miapi.item.modular;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.DurabilityProperty;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem extends VisualModularItem {

    static int getDurability(ItemStack stack){
        if (stack.getItem() instanceof VisualModularItem) {
            return (int) DurabilityProperty.property.getValueSafe(stack);
        }
        return stack.getMaxDamage();
    }
}
