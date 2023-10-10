package smartin.miapi.client.gui.crafting.crafter.create_module;

import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;

public class SingleCreateView extends InteractAbleWidget {

    protected SingleCreateView(int x, int y, int width, int height, CreateItemOption.CreateItem createItem) {
        super(x, y, width, height, Text.empty());
    }
}
