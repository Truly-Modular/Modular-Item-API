package smartin.miapi.item.modular.items;

import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularHelmet extends ArmorItem implements ModularItem {
    public ModularHelmet() {
        super(new ModularArmorMaterial(), Type.HELMET, new Settings());
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
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float)stack.getDamage()) / ModularItem.getDurability(stack));
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getEnchantability() {
        return 1;
    }
}
