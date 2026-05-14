package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.cache.ToolAndArmorProvider;

public interface ModularSetableArmorMaterial extends ItemStackSensitive {
    void setArmorMaterial(ArmorMaterial toolMaterial);

    default void lastItemStack(ItemStack itemStack) {
        ArmorMaterial material = ((ToolAndArmorProvider) (Object) itemStack).getArmorMaterial();
        setArmorMaterial(material);
    }
}