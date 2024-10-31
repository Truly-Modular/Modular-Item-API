package smartin.miapi.client.gui.crafting.crafter.help.pages;

import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.help.HelpPage;

import java.util.function.Consumer;

public class MaterialPreviewPage extends HelpPage {
    public MaterialPreviewPage(int x, int y, int width, int height, Text title, Consumer<InteractAbleWidget> remove) {
        super(x, y, width, height, title, remove, getPages("material", 4));
    }
}
