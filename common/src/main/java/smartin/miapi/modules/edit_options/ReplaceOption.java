package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.CraftEditOption;
import smartin.miapi.craft.CraftAction;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReplaceOption implements EditOption {
    public static ItemStack hoverStack = null;
    private static EditContext unsafeEditContext;

    @Override
    public ItemStack preview(PacketByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(editContext.getLinkedInventory().getStack(0));
        Inventory inventory = editContext.getLinkedInventory();
        if (hoverStack != null && !hoverStack.isEmpty()) {
            inventory = new SimpleInventory(2);
            inventory.setStack(1, hoverStack);
        }
        action.linkInventory(inventory, 1);
        return action.getPreview();
    }

    public static void tryPreview(PacketByteBuf buf) {
        if (unsafeEditContext != null) {
            unsafeEditContext.preview(buf);
        }
    }

    @Override
    public ItemStack execute(PacketByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(editContext.getLinkedInventory().getStack(0));
        action.linkInventory(editContext.getLinkedInventory(), 1);
        if (action.canPerform()) {
            return action.perform();
        } else {
            Miapi.LOGGER.warn("Could not previewStack Craft Action. This might indicate an exploit by " + editContext.getPlayer().getUuidAsString());
            return editContext.getItemstack();
        }
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return editContext.getSlot() != null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        hoverStack = null;
        unsafeEditContext = editContext;
        return new CraftEditOption(x, y, width, height, editContext);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25, 512, 512, this);
    }
}
