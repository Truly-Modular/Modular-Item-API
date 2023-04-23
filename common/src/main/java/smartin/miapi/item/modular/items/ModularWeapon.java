package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.DisplayNameProperty;

public class ModularWeapon extends Item implements ModularItem {
    public ModularWeapon() {
        super(new Settings().fireproof().maxCount(1).maxDamage(500).rarity(Rarity.COMMON));
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
