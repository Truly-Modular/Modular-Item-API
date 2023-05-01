package smartin.miapi.item.modular.items;

import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.DisplayNameProperty;

public class ExampleModularBowItem extends BowItem implements ModularItem {
    public ExampleModularBowItem() {
        super(new Item.Settings()
                .group(ItemGroup.COMBAT) // sets the creative tab for the item
                .maxCount(1) // sets the maximum stack size for the item
                .maxDamage(384) // sets the maximum durability of the item (bows have 384 uses)
                .rarity(Rarity.COMMON));
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
