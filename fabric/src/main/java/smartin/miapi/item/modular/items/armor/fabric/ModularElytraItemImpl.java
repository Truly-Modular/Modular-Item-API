package smartin.miapi.item.modular.items.armor.fabric;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;

public class ModularElytraItemImpl extends ModularElytraItem implements FabricElytraItem {

    public ModularElytraItemImpl() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    public static ModularElytraItem getInstance() {
        return new ModularElytraItemImpl();
    }
}
