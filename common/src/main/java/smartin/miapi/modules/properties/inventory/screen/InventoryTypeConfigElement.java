package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;

/**
 * width is forced to be 45 to fit the designated place.
 */
public abstract class InventoryTypeConfigElement extends InteractAbleWidget {

    protected InventoryTypeConfigElement(int x, int y, int height, Component title) {
        super(x, y, 45, height, Component.empty());
    }
}