package smartin.miapi.client.gui.crafting.crafter.help;

import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.help.pages.CreateItemsPage;
import smartin.miapi.client.gui.crafting.crafter.help.pages.MaterialPreviewPage;
import smartin.miapi.client.gui.crafting.crafter.help.pages.ModifyPage;

import java.util.function.Consumer;

public class HelpGuiInfo extends InteractAbleWidget {

    public HelpGuiInfo(int x, int y, int width, int height, Text title, Consumer<InteractAbleWidget> setMain, Consumer<InteractAbleWidget> remove) {
        super(x, y, width, height, title);
        ScrollingTextWidget header = new ScrollingTextWidget(x + 5, y + 3, width - 10, Text.translatable("miapi.gui.helper.header"));
        this.addChild(header);

        CraftingScreen craftingScreen = CraftingScreen.getInstance();
        int backgroundWidth = craftingScreen.getBackgroundWidth();

        int backgroundHeight = craftingScreen.getBackgroundHeight();

        int i = (craftingScreen.width - craftingScreen.getBackgroundWidth()) / 2;
        int j = (craftingScreen.height - craftingScreen.getBackgroundHeight()) / 2;
        this.addChild(new SimpleButton<>(x + 5, y + 20, width - 10, 18, Text.translatable("miapi.gui.helper.create_new_item"), null, (nextGUI) -> {
            setMain.accept(new CreateItemsPage(i, j, backgroundWidth, backgroundHeight, Text.translatable("miapi.help.create"), remove));
        }));
        this.addChild(new SimpleButton<>(x + 5, y + 40, width - 10, 18, Text.translatable("miapi.gui.helper.modify_item"), null, (nextGUI) -> {
            setMain.accept(new ModifyPage(i, j, backgroundWidth, backgroundHeight, Text.translatable("miapi.help.modify"), remove));
        }));
        this.addChild(new SimpleButton<>(x + 5, y + 60, width - 10, 18, Text.translatable("miapi.gui.helper.material_preview"), null, (nextGUI) -> {
            setMain.accept(new MaterialPreviewPage(i, j, backgroundWidth, backgroundHeight, Text.translatable("miapi.help.material"), remove));
        }));
    }
}
