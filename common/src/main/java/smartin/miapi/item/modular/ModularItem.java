package smartin.miapi.item.modular;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.DurabilityProperty;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem extends VisualModularItem {

    static int getDurability(ItemStack stack) {
        if (stack.getItem() instanceof VisualModularItem) {
            return DurabilityProperty.property.getValue(stack).orElse(1.0).intValue();
        }
        return stack.getMaxDamage();
    }

    static boolean isModularItem(ItemStack itemStack) {
        return itemStack.has(ModuleInstance.MODULE_INSTANCE_COMPONENT) && itemStack.get(ModuleInstance.MODULE_INSTANCE_COMPONENT).module != ItemModule.empty;
    }
}
