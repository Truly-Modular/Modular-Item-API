package smartin.miapi.item.modular.items.armor.neoforge;

import net.minecraft.world.item.Item;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;

public class ModularElytraItemImpl extends ModularElytraItem {

    public ModularElytraItemImpl() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    public static ModularElytraItem getInstance() {
        return new ModularElytraItemImpl();
    }
}
