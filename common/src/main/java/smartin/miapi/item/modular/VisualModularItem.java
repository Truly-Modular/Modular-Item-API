package smartin.miapi.item.modular;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.Objects;

public interface VisualModularItem {

    static boolean isVisualModularItem(ItemStack itemStack) {
        return isVisualModularItem(itemStack, itemStack.getItem());
    }

    static boolean isVisualModularItem(ItemStack itemStack, Item item) {
        return itemStack.has(ModuleInstance.MODULE_INSTANCE_COMPONENT) &&
               Objects.requireNonNull(itemStack.get(ModuleInstance.MODULE_INSTANCE_COMPONENT)).module != ItemModule.empty
               && item instanceof VisualModularItem;
    }
}
