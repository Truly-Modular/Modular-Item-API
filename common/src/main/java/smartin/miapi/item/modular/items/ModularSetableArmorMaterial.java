package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public interface ModularSetableArmorMaterial extends ItemStackSensitive {
    void setArmorMaterial(ArmorMaterial toolMaterial);

    Map<ItemStack, ArmorMaterial> ITEMSTACK_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    default void lastItemStack(ItemStack itemStack) {
        ArmorMaterial material = ITEMSTACK_CACHE.computeIfAbsent(itemStack, ModularArmorMaterial::forItem);
        setArmorMaterial(material);
    }
}