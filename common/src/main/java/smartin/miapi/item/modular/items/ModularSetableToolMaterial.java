package smartin.miapi.item.modular.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

public interface ModularSetableToolMaterial extends ItemStackSensitive{
    void lastItemStack(ToolMaterial toolMaterial);

    default void lastItemStack(ItemStack itemStack) {
        lastItemStack(MiningLevelProperty.getFakeToolMaterial(itemStack));
    }
}
