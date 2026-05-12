package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public interface ModularSetableArmorMaterial extends ItemStackSensitive {
    void setArmorMaterial(ArmorMaterial toolMaterial);

    Map<ModularSetableArmorMaterial, ModularArmorMaterial.StackBackedArmorMaterial> ITEMSTACK_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    default void lastItemStack(ItemStack itemStack) {
        ArmorMaterial material = ITEMSTACK_CACHE.computeIfAbsent(this, ignored -> ModularArmorMaterial.forItem()).withItemStack(itemStack);
        setArmorMaterial(material);
    }

    static void clearItemStack(ItemStack itemStack) {
        synchronized (ITEMSTACK_CACHE) {
            ITEMSTACK_CACHE.values().forEach(material -> material.clearIfItemStack(itemStack));
        }
    }

    static void clearArmorMaterialCache() {
        synchronized (ITEMSTACK_CACHE) {
            ITEMSTACK_CACHE.values().forEach(ModularArmorMaterial.StackBackedArmorMaterial::clearItemStack);
            ITEMSTACK_CACHE.clear();
        }
    }
}
