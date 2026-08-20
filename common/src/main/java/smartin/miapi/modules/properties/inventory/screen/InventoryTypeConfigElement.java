package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;

public abstract class InventoryTypeConfigElement extends InteractAbleWidget {

    protected InventoryTypeConfigElement(int x, int y, int width, int height, Component title) {
        super(x, y, width, height, Component.empty());
    }
}