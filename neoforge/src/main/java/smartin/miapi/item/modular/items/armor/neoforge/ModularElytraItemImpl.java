package smartin.miapi.item.modular.items.armor.neoforge;

import net.minecraft.world.item.Item;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;

import java.util.UUID;

public class ModularElytraItemImpl extends ModularElytraItem {
    private static UUID uuid = UUID.randomUUID();

    public ModularElytraItemImpl() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    public static ModularElytraItem getInstance() {
        return new ModularElytraItemImpl();
    }
}
