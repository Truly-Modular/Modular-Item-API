package smartin.miapi.item.modular.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularElytraItem extends ArmorItem implements ModularItem {
    public ModularElytraItem(Settings settings) {
        super(new ModularArmorMaterial(), Type.CHESTPLATE, settings);
    }

    @ExpectPlatform
    public static ModularElytraItem getInstance() {
        return new ModularElytraItem(new Settings().maxCount(1).fireproof());
    }


    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }
}
