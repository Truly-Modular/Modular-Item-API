package smartin.miapi.client.gui.crafting.moduleCrafter;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DetailView extends InteractAbleWidget {
    BoxList list;
    private ItemModule.ModuleInstance selected;
    private List<Consumer<ItemModule.ModuleInstance>> callbacks = new ArrayList<>();
    private Consumer<ItemModule.ModuleInstance> edit;
    private Consumer<SlotProperty.ModuleSlot> replace;

    public DetailView(int x, int y, int width, int height, ItemModule.ModuleInstance instance, Consumer<ItemModule.ModuleInstance> edit, Consumer<SlotProperty.ModuleSlot> replace) {
        super(x, y, width, height, Text.empty());
        list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        this.edit = edit;
        this.replace = replace;
        this.setSelected(instance);
    }

    public void registerCallBack(Consumer<ItemModule.ModuleInstance> callback) {
        callbacks.add(callback);
    }

    public void select(ItemModule.ModuleInstance instance) {
        this.selected = instance;
    }

    private void setSelected(ItemModule.ModuleInstance instance) {
        this.selected = instance;
        callbacks.forEach(callback -> {
            callback.accept(instance);
        });
        update();
    }

    private void update() {
        list.children().clear();
        List<ClickableWidget> boxList = new ArrayList<>();
        SimpleButton editButton = new SimpleButton(this.x + this.width - 80, this.y + this.height - 10, 20, 10, Text.literal("Edit"), selected, (instance) -> {
            Miapi.LOGGER.error("clickedEdit2");
            edit.accept((ItemModule.ModuleInstance) instance);
        });
        SimpleButton replaceButton = new SimpleButton(this.x + this.width - 55, this.y + this.height - 10, 50, 10, Text.literal("Replace"), selected, (instance) -> {
            replace.accept(SlotProperty.getSlotIn((ItemModule.ModuleInstance) instance));
        });
        this.addChild(editButton);
        this.addChild(replaceButton);
        if (selected.parent != null) {
            boxList.add(new SimpleButton(0, 0, this.width - 10, 10,Text.literal(selected.parent.toString()),selected.parent, (newInstance)->{
                setSelected((ItemModule.ModuleInstance) newInstance);
            }));
        }
        SlotProperty.getSlots(selected).forEach((integer, moduleSlot) -> {
            if(moduleSlot.inSlot!=null){
                boxList.add(new SimpleButton(0, 0, this.width - 10, 10,Text.literal(moduleSlot.inSlot.toString()),moduleSlot.inSlot, (newInstance)->{
                    setSelected((ItemModule.ModuleInstance) newInstance);
                }));
            }
            else{
                boxList.add(new SimpleButton(0, 0, this.width - 10, 10,Text.literal("Empty Slot"),moduleSlot, (slot)->{
                    replace.accept((SlotProperty.ModuleSlot) slot);
                }));
            }
        });
        list.setWidgets(boxList, 1);
    }
}
