package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.Wearable;
import smartin.miapi.item.modular.ModularItem;

public class ModularHelmet extends Item implements ModularItem, Wearable {
    public ModularHelmet() {
        super(new Item.Settings());
        Settings settings = new Item.Settings();
    }
}
