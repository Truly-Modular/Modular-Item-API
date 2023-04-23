package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.DisplayNameProperty;

public class ExampleModularItem extends Item implements ModularItem {
    public ExampleModularItem() {
        super(new Item.Settings());
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
