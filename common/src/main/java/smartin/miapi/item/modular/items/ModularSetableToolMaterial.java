package smartin.miapi.item.modular.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public interface ModularSetableToolMaterial extends ItemStackSensitive{
    void lastItemStack(ToolMaterial toolMaterial);

    Map<ItemStack, ToolMaterial> ITEMSTACK_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    default void lastItemStack(ItemStack itemStack) {
        ToolMaterial material = ITEMSTACK_CACHE.computeIfAbsent(itemStack, MiningLevelProperty::getFakeToolMaterial);
        lastItemStack(material);
    }
}
