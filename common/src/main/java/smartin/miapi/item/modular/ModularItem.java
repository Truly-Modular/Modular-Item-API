package smartin.miapi.item.modular;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.DurabilityProperty;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem extends VisualModularItem {

    static int getDurability(ItemStack stack) {
        if (VisualModularItem.isVisualModularItem(stack)) {
            return DurabilityProperty.property.getValue(stack).orElse(1.0).intValue();
        }
        return stack.getMaxDamage();
    }

    static boolean isModularItem(ItemStack itemStack) {
        return isModularItem(itemStack, itemStack.getItem());
    }

    static boolean isModularItem(ItemStack itemStack, Item item) {
        if (item instanceof ModularItem) {
            ModuleInstance moduleInstance = itemStack.get(ModuleInstance.MODULE_INSTANCE_COMPONENT);
            return moduleInstance != null;
        }
        return false;
    }
}
