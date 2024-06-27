package smartin.miapi.client.gui.crafting.crafter.help.pages;

import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.help.HelpPage;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class MaterialPreviewPage extends HelpPage {
    public MaterialPreviewPage(int x, int y, int width, int height, Component title, Consumer<InteractAbleWidget> remove) {
        super(x, y, width, height, title, remove, getPages("material", 4));
    }
}
