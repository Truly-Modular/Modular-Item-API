package smartin.miapi.client.gui.crafting.crafter.help.pages;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.help.HelpPage;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CreateItemsPage extends HelpPage {
    public CreateItemsPage(int x, int y, int width, int height, Component title, Consumer<InteractAbleWidget> remove) {
        super(x, y, width, height, title, remove, getPages("create", 2));
    }
}
