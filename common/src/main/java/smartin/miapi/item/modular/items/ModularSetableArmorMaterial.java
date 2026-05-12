package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.cache.ModularItemCache;

public interface ModularSetableArmorMaterial extends ItemStackSensitive {
    void setArmorMaterial(ArmorMaterial toolMaterial);

    String CACHE_KEY = "modular_setable_armor_fake";

    default void lastItemStack(ItemStack itemStack) {
        ArmorMaterial material = ModularItemCache.getRaw(itemStack,CACHE_KEY);
        setArmorMaterial(material);
    }
}