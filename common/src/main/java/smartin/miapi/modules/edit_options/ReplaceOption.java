package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.CraftEditOption;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReplaceOption implements EditOption {
    @Nullable
    public static EditContext unsafeEditContext;
    @Nullable
    public static CraftAction unsafeCraftAction;

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        if (editContext.getLinkedInventory() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = editContext.getLinkedInventory().getItem(0);
        action.setItem(itemStack);
        Container inventory = editContext.getLinkedInventory();
        boolean hasPreviewMaterial = false;
        if (
                PreviewManager.currentPreviewMaterial != null
        ) {
            hasPreviewMaterial = true;
            inventory = new SimpleContainer(2);
            PreviewManager.currentPreviewMaterialStack.getDamageValue();
            inventory.setItem(1, PreviewManager.currentPreviewMaterialStack);
        }
        action.linkInventory(inventory, 1);

        ItemStack preview = action.getPreview();
        if (editContext.getInstance() != null && !hasPreviewMaterial) {
            Material material = MaterialProperty.getMaterial(editContext.getInstance());
            if (material != null) {
                List<String> position = new ArrayList<>();
                editContext.getInstance().calculatePosition(position);
                ModuleInstance root = ItemModule.getModules(preview);
                ModuleInstance editing = root.getPosition(position);
                if (MaterialProperty.getMaterial(editing) == null) {
                    MaterialProperty.setMaterial(editing, material.getID());
                    editing.getRoot().writeToItem(preview);
                }
            }
        }
        return preview;
    }

    public static void tryPreview() {
        Miapi.LOGGER.info("try preview " + (unsafeEditContext != null) + " " + (unsafeCraftAction != null));
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
    public ItemStack execute(FriendlyByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(editContext.getLinkedInventory().getItem(0));
        action.linkInventory(editContext.getLinkedInventory(), 1);
        if (action.canPerform()) {
            return action.perform();
        } else {
            Miapi.LOGGER.warn("Could not previewStack Craft Action. This might indicate an exploit by " + editContext.getPlayer().getStringUUID());
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
