package smartin.miapi.item.modular.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public interface ModularSetableToolMaterial extends ItemStackSensitive{
    void lastItemStack(ToolMaterial toolMaterial);

    Map<ModularSetableToolMaterial, MiningLevelProperty.StackBackedToolMaterial> ITEMSTACK_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    default void lastItemStack(ItemStack itemStack) {
        ToolMaterial material = ITEMSTACK_CACHE.computeIfAbsent(this, ignored -> MiningLevelProperty.getFakeToolMaterial()).withItemStack(itemStack);
        lastItemStack(material);
    }

    static void clearItemStack(ItemStack itemStack) {
        synchronized (ITEMSTACK_CACHE) {
            ITEMSTACK_CACHE.values().forEach(material -> material.clearIfItemStack(itemStack));
        }
    }

    static void clearToolMaterialCache() {
        synchronized (ITEMSTACK_CACHE) {
            ITEMSTACK_CACHE.values().forEach(MiningLevelProperty.StackBackedToolMaterial::clearItemStack);
            ITEMSTACK_CACHE.clear();
        }
    }
}
