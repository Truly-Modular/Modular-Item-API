package smartin.miapi.item.modular;

import net.minecraft.world.item.Item;

public class ModularItemPart extends Item implements VisualModularItem {
    public ModularItemPart() {
        super(new Properties().stacksTo(1));
    }
}
