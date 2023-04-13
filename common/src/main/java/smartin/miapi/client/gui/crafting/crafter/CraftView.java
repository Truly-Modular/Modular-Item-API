package smartin.miapi.client.gui.crafting.crafter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CraftView extends InteractAbleWidget {
    ItemStack compareStack;
    ItemStack originalStack;
    SlotProperty.ModuleSlot slotToChange;
    ItemModule replacingModule;
    String packetId;
    Inventory linkedInventory;
    CraftAction action;
    SimpleButton craftButton;
    int inventoryOffset;

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
    public CraftView(int x, int y, int width, int height, String packetId, ItemModule module, ItemStack stack, SlotProperty.ModuleSlot slot, Consumer<SlotProperty.ModuleSlot> back, Consumer<ItemStack> newStack) {
        super(x, y, width, height, Text.empty());
        compareStack = stack;
        originalStack = stack;
        slotToChange = slot;
        this.packetId = packetId;
        replacingModule = module;
        BoxList list = new BoxList(x, y, width, height - 11, Text.empty(), new ArrayList<>());
        addChild(list);
        action = new CraftAction(originalStack, slotToChange, module, MinecraftClient.getInstance().player);
        action.linkInventory(linkedInventory, inventoryOffset);
        list.children().clear();
        action.forEachCraftingProperty(originalStack, ((craftingProperty, moduleInstance, itemStacks, invStart, invEnd) -> {
            InteractAbleWidget guiScreen = craftingProperty.getGui();
            if (guiScreen != null) {
                guiScreen.x = this.x;
                guiScreen.y = this.y;
                guiScreen.setWidth(this.width);
                guiScreen.setHeight(this.height - 11);
                list.children().add(guiScreen);
                //TODO:Figure out sth for the slots so only the ones linked to this ui
            }
        }));
        craftButton = new SimpleButton<>(this.x + this.width - 50, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID + ".ui.craft"), null, (callback) -> {
            if (action.canPerform()) {
                Networking.sendC2S(packetId, action.toPacket(Networking.createBuffer()));
                newStack.accept(action.getPreview());
            }
        });
        addChild(craftButton);
        addChild(new SimpleButton<>(this.x + 10, this.y + this.height - 10, 40, 10, Text.translatable(Miapi.MOD_ID + ".ui.craft"), slot, back::accept));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        craftButton.isEnabled = action.canPerform();
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void linkInventory(Inventory inventory, int offset) {
        this.linkedInventory = inventory;
        this.inventoryOffset = offset;
    }

    public ItemStack compareStack() {
        return compareStack;
    }
}
