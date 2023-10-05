package smartin.miapi.fabric;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import smartin.miapi.item.modular.items.ModularElytraItem;

public class ModularElytraItemImpl extends ModularElytraItem implements FabricElytraItem {

    public ModularElytraItemImpl() {
        super(new Settings().maxCount(1).fireproof());
    }

    public static ModularElytraItem getInstance(){
        return new ModularElytraItemImpl();
    }
}
