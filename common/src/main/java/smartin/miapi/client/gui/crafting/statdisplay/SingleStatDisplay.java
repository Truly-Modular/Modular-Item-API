package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.item.ItemStack;

public interface SingleStatDisplay {

    boolean shouldRender(ItemStack original, ItemStack compareTo);

    default int getHeightDesired() {
        return 32;
    }

    default int getWidthDesired() {
        return 80;
    }
}
