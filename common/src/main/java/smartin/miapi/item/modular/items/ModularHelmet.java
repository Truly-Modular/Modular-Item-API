package smartin.miapi.item.modular.items;

import net.minecraft.item.*;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularHelmet extends ArmorItem implements ModularItem {
    public ModularHelmet() {
        super(new ModularArmorMaterial(), Type.HELMET, new Settings());
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
