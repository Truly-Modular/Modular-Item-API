package smartin.miapi.item.modular;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

public interface VisualModularItem {

    static boolean isModularItem(ItemStack itemStack){
        return itemStack.has(ModuleInstance.MODULE_INSTANCE_COMPONENT);
    }
}
