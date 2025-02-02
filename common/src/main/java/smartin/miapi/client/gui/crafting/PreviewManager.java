package smartin.miapi.client.gui.crafting;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;

public class PreviewManager {
    @Nullable
    public static Material currentPreviewMaterial = null;
    public static ItemStack currentPreviewMaterialStack = ItemStack.EMPTY;
    private static ItemStack cursorStack = ItemStack.EMPTY;

    private static Material lastFramePreviewMaterial = null;

    public static void setCursorItemstack(ItemStack itemstack) {
        if (cursorStack != itemstack) {
            Material material = MaterialProperty.getMaterialFromIngredient(itemstack);
            if (material != currentPreviewMaterial) {
                cursorStack = itemstack;
                updateMaterial(material, cursorStack);
            }
        }
    }

    public static void resetCursorStack() {
        setCursorItemstack(ItemStack.EMPTY);
    }

    public static void resetPreview() {
        ReplaceOption.resetPreview();
    }

    public static boolean hasValidPreview() {
        return PreviewManager.currentPreviewMaterial != null;
    }

    public static void updateMaterial(Material material, ItemStack itemStack) {
        currentPreviewMaterial = material;
        currentPreviewMaterialStack = itemStack;
    }

    public static void tick() {
        if (lastFramePreviewMaterial != currentPreviewMaterial) {
            lastFramePreviewMaterial = currentPreviewMaterial;
            if (CraftingScreen.getInstance() != null) {
                CraftingScreen craftingScreen = CraftingScreen.getInstance();
                ItemStack currentStack = craftingScreen.getItem();
                if (currentStack.isEmpty() && craftingScreen.getEditOption() == null) {
                    if (ModularItem.isModularItem(currentStack) || currentPreviewMaterial == null) {
                        craftingScreen.updatePreviewItemStack(ItemStack.EMPTY);
                    } else {
                        craftingScreen.updatePreviewItemStack(cursorStack);
                    }
                } else {
                    ReplaceOption.tryPreview();
                    String material = currentPreviewMaterial == null ? "empty" : currentPreviewMaterial.getID().toString();
                    Miapi.LOGGER.info("updating preview Material " + material + " " + hasValidPreview());
                }
            }
        }
    }
}
