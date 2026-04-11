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
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.EditModuleCrafter;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.network.Networking;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CosmeticEditOption implements EditOption {
    public static ItemStack hoverStack = ItemStack.EMPTY;
    public static int updateCount = 0;
    @Nullable
    public static EditContext unsafeEditContext;
    @Nullable
    public static CraftAction unsafeCraftAction;
    public String slotType = "cosmetic";

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench(), editContext.getScreenHandler());
        if (editContext.getLinkedInventory() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = editContext.getLinkedInventory().getItem(0);
        action.setItem(itemStack);
        Container inventory = editContext.getLinkedInventory();
        if (hoverStack != null && !hoverStack.isEmpty()) {
            boolean emptyMaterial = editContext.getLinkedInventory().getItem(1).isEmpty();

            inventory = new SimpleContainer(2);
            inventory.setItem(1, hoverStack);
        }
        action.linkInventory(inventory, 1);
        return action.getPreview();
    }

    public static void tryPreview() {
        if (unsafeEditContext != null && unsafeCraftAction != null) {
            try {
                unsafeEditContext.preview(unsafeCraftAction.toPacket(Networking.createBuffer()));
            } catch (Exception e) {

            }
        }
    }

    public static void setHoverStack(ItemStack itemStack, boolean safe) {
        updateCount = 2;
        if (itemStack != null && hoverStack != null) {
            if (ItemStack.matches(itemStack, hoverStack)) {
                return;
            }
        }
        if (!itemStack.isEmpty()) {
            Material material = MaterialProperty.getMaterialFromIngredient(itemStack);
            if (material != null) {
                hoverStack = itemStack;
                tryPreview();
                return;
            }
        }
        if (!safe) {
            hoverStack = itemStack;
            tryPreview();
        }
    }

    @Override
    public ItemStack execute(FriendlyByteBuf buffer, EditContext editContext) {
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench(), editContext.getScreenHandler());
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
        SlotProperty.ModuleSlot slot = editContext.getSlot();
        if (editContext.getInstance() != null) {
            slot = SlotProperty.getSlotIn(editContext.getInstance());
        }
        if (slot != null) {
            if (editContext.getSlot().slotType.equals(slotType)) {
                return true;
            }
        }
        if (editContext.getInstance() != null) {
            return SlotProperty.getSlots(editContext.getInstance()).values().stream()
                    .anyMatch(moduleSlot -> moduleSlot.slotType.equals(slotType));
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        hoverStack = null;
        unsafeEditContext = editContext;
        return new EditModuleCrafter(x, y, width, height, this, editContext, slotType);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        hoverStack = null;
        unsafeEditContext = null;
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339, 25 + 28 * 2 + 38 + 56, 512, 512, "miapi.ui.edit_option.hover.cosmetic", this);
    }
}
