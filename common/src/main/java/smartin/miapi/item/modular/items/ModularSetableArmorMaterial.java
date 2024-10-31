package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

public interface ModularSetableArmorMaterial extends ItemStackSensitive {
    void setArmorMaterial(ArmorMaterial toolMaterial);

    default void lastItemStack(ItemStack itemStack) {
        setArmorMaterial(ModularArmorMaterial.forItem(itemStack));
    }


}
