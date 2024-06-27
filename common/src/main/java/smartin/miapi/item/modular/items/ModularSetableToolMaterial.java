package smartin.miapi.item.modular.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

public interface ModularSetableToolMaterial {
    void setToolMaterial(Tier toolMaterial);

    default void setToolMaterial(ItemStack itemStack) {
        setToolMaterial(MiningLevelProperty.getFakeToolMaterial(itemStack));
    }
}
