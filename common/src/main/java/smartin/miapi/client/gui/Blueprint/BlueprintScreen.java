package smartin.miapi.client.gui.Blueprint;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.ParentHandledScreen;

public class BlueprintScreen extends ParentHandledScreen<BlueprintScreenHandler> implements ScreenHandlerProvider<BlueprintScreenHandler> {
    /**
     * This is a Handled Screen class that by default correctly handles Children.
     * If you want those Children to support Children the use of the InteractAbleWidget class is recommended
     *
     * @param handler   the ScreenHandler linked to this HandledScreen
     * @param inventory the PlayerInventory of the Player opening this screen
     * @param title     the Title of the Screen
     */
    protected BlueprintScreen(BlueprintScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

    }
}
