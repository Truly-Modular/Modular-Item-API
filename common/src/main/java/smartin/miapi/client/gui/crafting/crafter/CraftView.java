package smartin.miapi.client.gui.crafting.crafter;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.craft.Crafter;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CraftView extends InteractAbleWidget {
    ItemStack compareStack;
    ItemStack originalStack;
    SlotProperty.ModuleSlot slotToChange;
    ItemModule replacingModule;

    /**
     * This is a Widget build to support Children and parse the events down to them.
     * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
     * unlike the base vanilla classes.
     * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
     * super method or handle the children yourself
     *
     * @param x      the X Position
     * @param y      the y Position
     * @param width  the width
     * @param height the height
     *               These for Params above are used to create feedback on isMouseOver() by default
     */
    public CraftView(int x, int y, int width, int height, ItemModule module, ItemStack stack, SlotProperty.ModuleSlot slot,Consumer<SlotProperty.ModuleSlot> back, Consumer<ItemStack> newStack) {
        super(x, y, width, height, Text.empty());
        compareStack = stack;
        originalStack = stack;
        slotToChange = slot;
        replacingModule = module;
        BoxList list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        list.children().clear();
        SimpleButton craftButton = new SimpleButton<>(this.x + this.width - 50, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID+".ui.craft"), null, (callback) -> {
            Crafter.CraftAction action = new Crafter.CraftAction(originalStack,slotToChange,module,null);
            newStack.accept(action.perform());
            //newStack.accept(Crafter.craft(stack,slot,module));
        });
        addChild(craftButton);
        addChild(new SimpleButton<>(this.x + 10, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID+".ui.craft"), slot, back::accept));
    }

    public ItemStack compareStack(){
        return compareStack;
    }
}
