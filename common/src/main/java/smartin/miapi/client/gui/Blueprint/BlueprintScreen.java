package smartin.miapi.client.gui.Blueprint;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import smartin.miapi.client.gui.ParentHandledScreen;

public class BlueprintScreen extends ParentHandledScreen<BlueprintScreenHandler> implements MenuAccess<BlueprintScreenHandler> {
    /**
     * This is a Handled Screen class that by default correctly handles Children.
     * If you want those Children to support Children the use of the InteractAbleWidget class is recommended
     *
     * @param handler   the ScreenHandler linked to this HandledScreen
     * @param inventory the PlayerInventory of the Player opening this screen
     * @param title     the Title of the Screen
     */
    protected BlueprintScreen(BlueprintScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {

    }
}
