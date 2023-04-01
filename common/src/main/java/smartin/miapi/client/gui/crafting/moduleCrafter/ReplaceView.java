package smartin.miapi.client.gui.crafting.moduleCrafter;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.AllowedSlots;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ReplaceView extends InteractAbleWidget {
    public ReplaceView(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, Consumer<ItemModule.ModuleInstance> back, Consumer<ItemModule> craft) {
        super(x, y, width, height, Text.empty());
        BoxList list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        list.children().clear();
        SimpleButton backButton = new SimpleButton(this.x + 2, this.y + this.height - 10, 20, 10, Text.literal("Edit"), slot.parent, (instance2) -> {
            Miapi.LOGGER.error("clickedBack2");
            back.accept(slot.parent);
        });
        addChild(backButton);
        SimpleButton noModule = new SimpleButton(this.x + this.width - 80, this.y + this.height - 10, 20, 10, Text.literal("no Module"), null, (module) -> {
            craft.accept((ItemModule) module);
        });
        ArrayList<ClickableWidget> toList = new ArrayList<>();
        toList.add(noModule);
        AllowedSlots.allowedIn(slot).forEach(module -> {
            toList.add(new SimpleButton(0, 0, this.width - 10, 10, Text.literal(module.getName()), module, (replaceModule) -> {
                Miapi.LOGGER.error("clicked Craft");
                craft.accept((ItemModule) replaceModule);
            }));
        });
        list.setWidgets(toList,1);
    }
}
