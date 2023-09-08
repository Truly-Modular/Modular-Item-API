package smartin.miapi.client.gui.old;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;

@Environment(EnvType.CLIENT)
public interface SingleStatDisplay {

    boolean shouldRender(ItemStack original, ItemStack compareTo);

    default int getHeightDesired() {
        return 32;
    }

    default int getWidthDesired() {
        return 80;
    }

    InteractAbleWidget getHoverWidget();
}
