package smartin.miapi.client.gui;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

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
            ReplaceOption.tryPreview();
            String material = currentPreviewMaterial == null ? "empty" : currentPreviewMaterial.getKey();
            Miapi.LOGGER.info("updating preview Material " + material + " " + hasValidPreview());
        }
    }
}
