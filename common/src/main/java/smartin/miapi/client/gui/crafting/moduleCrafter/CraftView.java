package smartin.miapi.client.gui.crafting.moduleCrafter;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.craft.Crafter;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CraftView extends InteractAbleWidget {
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
    public CraftView(int x, int y, int width, int height, ItemModule module, ItemStack stack, SlotProperty.ModuleSlot slot, Consumer<ItemStack> newStack) {
        super(x, y, width, height, Text.empty());
        BoxList list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        list.children().clear();
        SimpleButton craftButton = new SimpleButton(this.x + this.width - 80, this.y + this.height - 10, 20, 10, Text.literal("Craft"), null, (instance2) -> {
            newStack.accept(Crafter.craft(stack,slot,module));
        });
        addChild(craftButton);
    }

    public ItemStack craft(ItemStack stack, SlotProperty.ModuleSlot slot, ItemModule module, ItemModule.ModuleInstance instance) {
        ItemStack replaceStack = stack.copy();
        replaceStack.getNbt().remove(ModularItemCache.cacheKey);
        if (slot == null) {
            //remove/replace base Module
            if (module == null) {
                replaceStack.getNbt().putString("modules", "{module:}");
            } else {
                replaceStack.getNbt().putString("modules", "{module:" + module.getName() + "}");
            }
        } else {
            ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(stack);
            ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
            List<Integer> location = new ArrayList<>();
            while (instance.parent != null) {
                int slotNumber = SlotProperty.getSlotNumberIn(instance);
                location.add(slotNumber);
                Miapi.LOGGER.error(String.valueOf(slotNumber));
                instance = instance.parent;
            }
            ItemModule.ModuleInstance parsingInstance = newBaseModule;
            for (int i = location.size() - 1; i > 0; i--) {
                Miapi.LOGGER.warn(String.valueOf(location.get(i)));
                Miapi.LOGGER.error(newBaseModule.toString());
                parsingInstance = parsingInstance.subModules.get(location.get(i));
            }
            if (module == null) {
                parsingInstance.subModules.remove(location.get(0));
            } else {
                parsingInstance.subModules.put(location.get(0), new ItemModule.ModuleInstance(module));
            }
            replaceStack.getNbt().putString("modules", newBaseModule.toString());
        }
        return replaceStack;
    }
}
