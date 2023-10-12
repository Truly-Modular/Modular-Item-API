package smartin.miapi.item.modular.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularBoots extends ArmorItem implements ModularItem {
    public ModularBoots() {
        super(new ModularArmorMaterial(), Type.BOOTS, new Settings());
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack){
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamage() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getEnchantability() {
        return 1;
    }
}
