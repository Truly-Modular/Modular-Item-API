package smartin.miapi.item.modular.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

public interface ModularSetableToolMaterial {
    void setToolMaterial(ToolMaterial toolMaterial);

    default void setToolMaterial(ItemStack itemStack) {
        setToolMaterial(MiningLevelProperty.getFakeToolMaterial(itemStack));
    }
}
