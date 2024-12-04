package smartin.miapi.client.gui.crafting;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

public class PreviewManager {
    @Nullable
    public static Material currentPreviewMaterial = null;
    public static ItemStack currentPreviewMaterialStack = ItemStack.EMPTY;
    private static ItemStack cursorStack = ItemStack.EMPTY;

    private static Material lastFramePreviewMaterial = null;
    public static boolean isOpen = false;

    public static void setCursorItemstack(ItemStack itemstack) {
        if (isOpen && cursorStack != itemstack) {
            Material material = MaterialProperty.getMaterialFromIngredient(itemstack);
            if (material != currentPreviewMaterial) {
                cursorStack = itemstack;
                updateMaterial(material, cursorStack);
            }

            if (net.minecraft.client.MinecraftClient.getInstance().currentScreen instanceof CraftingScreen) {
                isOpen = false;
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
        isOpen = true;
        if (lastFramePreviewMaterial != currentPreviewMaterial) {
            lastFramePreviewMaterial = currentPreviewMaterial;
            if (CraftingScreen.getInstance() != null) {
                CraftingScreen craftingScreen = CraftingScreen.getInstance();
                ItemStack currentStack = craftingScreen.getItem();
                if (currentStack.isEmpty() && craftingScreen.getEditOption() == null) {
                    if (cursorStack.getItem() instanceof ModularItem || currentPreviewMaterial == null) {
                        craftingScreen.updatePreviewItemStack(ItemStack.EMPTY);
                    } else {
                        craftingScreen.updatePreviewItemStack(cursorStack);
                    }
                } else {
                    ReplaceOption.tryPreview();
                    String material = currentPreviewMaterial == null ? "empty" : currentPreviewMaterial.getKey();
                    //Miapi.LOGGER.info("updating preview Material " + material + " " + hasValidPreview());
                }
            }
        }
    }
}
