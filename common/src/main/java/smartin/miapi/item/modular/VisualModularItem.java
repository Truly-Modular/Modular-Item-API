package smartin.miapi.item.modular;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

public interface VisualModularItem {

    static boolean isVisualModularItem(ItemStack itemStack) {
        return isVisualModularItem(itemStack, itemStack.getItem());
    }

    static boolean isVisualModularItem(ItemStack itemStack, Item item) {
        return itemStack.has(ModuleInstance.MODULE_INSTANCE_COMPONENT)
               && item instanceof VisualModularItem;
    }
}
