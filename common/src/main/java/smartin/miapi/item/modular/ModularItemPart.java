package smartin.miapi.item.modular;

import net.minecraft.item.Item;

public class ModularItemPart extends Item implements VisualModularItem {
    public ModularItemPart() {
        super(new Settings().maxCount(1));
    }
}
