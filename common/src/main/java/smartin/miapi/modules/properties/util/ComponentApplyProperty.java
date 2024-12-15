package smartin.miapi.modules.properties.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

/**
 * this interface is for Properties to update Itemstack Component Data
 * it is called during crafting and can be called during other times.
 * It allows for all in-place Modifications of the Itemstack.
 * If you want to apply replacements of the ItemStack in question, have a look at
 * {@link CraftingProperty} instead, it is ONLY triggered during crafting actions
 */
public interface ComponentApplyProperty {

    /**
     * This should be called to update an ItemStacks Component
     *
     * @param toUpdate the Itemstack to be updated
     */
    static void updateItemStack(ItemStack toUpdate, @Nullable RegistryAccess registryAccess) {
        if (ReloadEvents.isInReload()) {
            return;
        }
        ModuleInstance module = ItemModule.getModules(toUpdate);
        if (module == null) {
            return;
        }
        if (module.registryAccess == null) {
            module.registryAccess = registryAccess;
        }
        module.clearCaches();
        RegistryInventory.moduleProperties
                .getFlatMap()
                .values()
                .stream()
                .filter(ComponentApplyProperty.class::isInstance)
                .map(ComponentApplyProperty.class::cast)
                .forEach(componentApplyProperty ->
                        componentApplyProperty.updateComponent(toUpdate, module.registryAccess));
    }

    /**
     * This needs to be implemented in the Property, it allows you to perform
     * in-place modifications of the Itemstack
     *
     * @param itemStack
     */
    void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess);
}
