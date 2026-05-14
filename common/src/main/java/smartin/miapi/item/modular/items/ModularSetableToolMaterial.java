package smartin.miapi.item.modular.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import smartin.miapi.modules.cache.ToolAndArmorProvider;

public interface ModularSetableToolMaterial extends ItemStackSensitive{
    void lastItemStack(ToolMaterial toolMaterial);

    String CACHE_KEY = "modular_setable_tool_fake";

    default void lastItemStack(ItemStack itemStack) {
        ToolMaterial material = ((ToolAndArmorProvider) (Object) itemStack).getToolMaterial();
        lastItemStack(material);
    }
}
