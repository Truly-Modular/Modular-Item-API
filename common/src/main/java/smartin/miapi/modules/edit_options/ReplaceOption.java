package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.CraftEditOption;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReplaceOption implements EditOption {
    private static final String MODERN_MODE_KEY = "superior_miapi.modern_mode";
    private static final String INVENTORY_OFFSET_KEY = "superior_miapi.inventory_offset";
    private static final String RETURN_LEFTOVER_TO_INVENTORY_KEY = "superior_miapi.return_leftover_material_to_inventory";

    @Nullable
    public static EditContext unsafeEditContext;
    @Nullable
    public static CraftAction unsafeCraftAction;

    @Override
    public ItemStack preview(PacketByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        if (editContext.getLinkedInventory() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = editContext.getLinkedInventory().getStack(0);
        action.setItem(itemStack);
        Inventory inventory = editContext.getLinkedInventory();
        boolean hasPreviewMaterial = false;
        if (
                PreviewManager.currentPreviewMaterial != null
        ) {
            hasPreviewMaterial = true;
            inventory = new SimpleInventory(2);
            PreviewManager.currentPreviewMaterialStack.getDamage();
            inventory.setStack(1, PreviewManager.currentPreviewMaterialStack);
        }
        action.linkInventory(inventory, 1);

        ItemStack preview = action.getPreview();
        if (editContext.getInstance() != null && !hasPreviewMaterial) {
            Material material = MaterialProperty.getMaterial(editContext.getInstance());
            if (material != null) {
                List<Integer> position = new ArrayList<>();
                editContext.getInstance().calculatePosition(position);
                ItemModule.ModuleInstance root = ItemModule.getModules(preview);
                ItemModule.ModuleInstance editing = root.getPosition(position);
                if (MaterialProperty.getMaterial(editing) == null) {
                    MaterialProperty.setMaterial(editing, material.getKey());
                    editing.getRoot().writeToItem(preview);
                }
            }
        }
        return preview;
    }

    public static void tryPreview() {
        //Miapi.LOGGER.info("try preview " + (unsafeEditContext != null) + " " + (unsafeCraftAction != null));
        if (unsafeEditContext != null && unsafeCraftAction != null) {
            try {
                unsafeEditContext.preview(unsafeCraftAction.toPacket(Networking.createBuffer()));
            } catch (Exception e) {

            }
        }
    }

    public static void resetPreview() {
        ReplaceOption.unsafeEditContext = null;
        ReplaceOption.unsafeCraftAction = null;
    }

    @Override
    public ItemStack execute(PacketByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(editContext.getLinkedInventory().getStack(0));
        action.linkInventory(editContext.getLinkedInventory(), this.resolveLinkedInventoryOffset(action, editContext));
        if (action.canPerform()) {
            ItemStack crafted = action.perform();
            this.returnModernMaterialRemainder(action, editContext);
            return crafted;
        } else {
            Miapi.LOGGER.warn("Could not previewStack Craft Action. This might indicate an exploit by " + editContext.getPlayer().getUuidAsString());
            return editContext.getItemstack();
        }
    }

    private void returnModernMaterialRemainder(final CraftAction action, final EditContext editContext) {
        if (action == null || action.data == null || editContext == null || editContext.getLinkedInventory() == null || editContext.getPlayer() == null) {
            return;
        }
        if (!Boolean.parseBoolean(action.data.getOrDefault(MODERN_MODE_KEY, "false"))
            || !Boolean.parseBoolean(action.data.getOrDefault(RETURN_LEFTOVER_TO_INVENTORY_KEY, "false"))) {
            return;
        }
        final String rawOffset = action.data.get(INVENTORY_OFFSET_KEY);
        if (rawOffset == null || rawOffset.isBlank()) {
            return;
        }
        final int offset;
        try {
            offset = Integer.parseInt(rawOffset.trim());
        } catch (NumberFormatException ignored) {
            return;
        }
        if (offset <= 0 || offset >= editContext.getLinkedInventory().size()) {
            return;
        }
        final ItemStack remainder = editContext.getLinkedInventory().getStack(offset);
        if (remainder.isEmpty()) {
            return;
        }
        final ItemStack toMove = remainder.copy();
        if (!editContext.getPlayer().getInventory().insertStack(toMove) && !toMove.isEmpty()) {
            editContext.getPlayer().dropItem(toMove, false);
        }
        editContext.getLinkedInventory().setStack(offset, ItemStack.EMPTY);
        editContext.getLinkedInventory().markDirty();
    }

    private int resolveLinkedInventoryOffset(final CraftAction action, final EditContext editContext) {
        if (action == null || action.data == null || editContext == null || editContext.getLinkedInventory() == null) {
            return 1;
        }
        if (!Boolean.parseBoolean(action.data.getOrDefault(MODERN_MODE_KEY, "false"))) {
            return 1;
        }
        final String rawOffset = action.data.get(INVENTORY_OFFSET_KEY);
        if (rawOffset == null || rawOffset.isBlank()) {
            return 1;
        }
        final int offset;
        try {
            offset = Integer.parseInt(rawOffset.trim());
        } catch (NumberFormatException ignored) {
            return 1;
        }
        return offset > 0 && offset < editContext.getLinkedInventory().size() ? offset : 1;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return editContext.getSlot() != null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        unsafeEditContext = editContext;
        return new CraftEditOption(x, y, width, height, editContext);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        unsafeEditContext = null;
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25, 512, 512, "miapi.ui.edit_option.hover.replace", this);
    }
}
