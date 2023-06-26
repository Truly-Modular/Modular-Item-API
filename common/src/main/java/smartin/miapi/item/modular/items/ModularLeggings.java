package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularLeggings extends ArmorItem implements ModularItem {
    public ModularLeggings() {
        super(new ModularArmorMaterial(),Type.LEGGINGS,new Settings());
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
