package smartin.miapi.client.gui.crafting;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.material.properties.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.ReplaceOption;

public class PreviewManager {
    @Nullable
    public static Material currentPreviewMaterial = null;
    public static ItemStack currentPreviewMaterialStack = ItemStack.EMPTY;
    private static ItemStack cursorStack = ItemStack.EMPTY;

    private static Material lastFramePreviewMaterial = null;
    private static int noUpdate = 0;

    public static void setCursorItemstack(ItemStack itemstack) {
        if (Environment.isClient()) {
            if (Minecraft.getInstance().isSameThread()) {
                if (cursorStack != itemstack ||
                    cursorStack != null &&
                    itemstack != null &&
                    cursorStack.equals(itemstack)
                ) {
                    Material material = MaterialProperty.getMaterialFromIngredient(itemstack);
                    if (material != currentPreviewMaterial) {
                        cursorStack = itemstack;
                        updateMaterial(material, cursorStack);
                    }else{
                        noUpdate = 0;
                    }
                } else {
                    noUpdate = 0;
                }
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
        noUpdate++;
        if (lastFramePreviewMaterial != currentPreviewMaterial) {
            lastFramePreviewMaterial = currentPreviewMaterial;
            noUpdate = 0;
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
                }
            }
        } else {
            if (noUpdate > 5 && currentPreviewMaterial != null) {
                if (CraftingScreen.getInstance() != null) {
                    CraftingScreen craftingScreen = CraftingScreen.getInstance();
                    if (craftingScreen != null) {
                        ItemStack currentStack = craftingScreen.getItem();
                        if (currentStack.isEmpty() && !(craftingScreen.getEditOption() instanceof CreateItemOption)) {
                            craftingScreen.updatePreviewItemStack(ItemStack.EMPTY);
                        } else {
                            ReplaceOption.tryPreview();
                            currentPreviewMaterial = null;
                        }
                    }
                }
            }
        }
    }
}
