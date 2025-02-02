package smartin.miapi.client.gui.crafting.crafter.replace.hover;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.composite.AnyIngredientComposite;
import smartin.miapi.material.composite.CompositeMaterial;
import smartin.miapi.material.composite.MaterialOverwriteComposite;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.registries.RegistryInventory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoverMaterialList extends InteractAbleWidget {
    public Map<String, List<Material>> materials = new LinkedHashMap<>();
    public List<String> materialKeys;
    public int selected = 0;
    public int scrollPosOne = 0;
    public int scrollPosTwo = 0;
    protected final int maxElements = 8;
    final int selectedColor = FastColor.ARGB32.color(255, 255, 255, 255);
    final int unselectedColor = FastColor.ARGB32.color(255, 200, 200, 200);
    final int moreEntryColor = FastColor.ARGB32.color(255, 160, 160, 160);
    protected int start = 0;
    protected int end = 0;
    protected boolean lastRendered = false;
    protected Material previewMaterial = null;
    protected int realMouseX = 0;
    protected int realMouseY = 0;
    protected int sizeDetailList, verticalSize, sizeBaseList = 0;
    protected boolean permaOpen = false;

    public HoverMaterialList(ItemModule module, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        materialKeys = AllowedMaterial.property.getAllowedKeys(module);
        for (String key : materialKeys) {
            materials.put(key, AllowedMaterial.property.getMaterials(key)
                    .stream()
                    .sorted(Comparator.comparing(m -> m.getTranslation().getString()))
                    .toList());
        }
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.blit(CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 404, 96, 20, 11, 512, 512);
        super.renderWidget(drawContext, mouseX, mouseY, delta);
        if (lastRendered || permaOpen) {
            selectedMaterialUpdate(previewMaterial);
        }
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        int currentY = this.getY() + 3;
        if (materialKeys.isEmpty()) {
            return;
        }
        String selectedMaterialOrGroup = materialKeys.get(selected + scrollPosOne);
        List<Material> materialList = materials.get(selectedMaterialOrGroup);
        sizeBaseList = 30;
        verticalSize = Math.min(materials.size(), maxElements);
        verticalSize = Math.clamp(materialList.size(), verticalSize, maxElements);
        verticalSize *= 14;
        for (int i = scrollPosOne; i < Math.min(materials.size(), maxElements + scrollPosOne); i++) {
            Component material = getTranslation(materialKeys.get(i));
            sizeBaseList = Math.max(Minecraft.getInstance().font.width(material), sizeBaseList);
        }
        sizeDetailList = 0;
        if (materialList.size() > 1) {
            for (Material m : materialList) {
                Component material = m.getTranslation();
                sizeDetailList = Math.max(Minecraft.getInstance().font.width(material), sizeDetailList);
            }
        }
        sizeDetailList += 10;
        if (isMouseOver(realMouseX, realMouseY) || permaOpen) {
            lastRendered = true;
            RenderSystem.disableDepthTest();
            drawContext.fill(getX(), getY(), getX() + sizeDetailList + sizeBaseList, getY() + verticalSize, FastColor.ARGB32.color(210, 0, 0, 0));
            if (!materials.isEmpty()) {
                scrollPosOne = Math.max(0, Math.min(materials.size() - maxElements - 1, scrollPosOne));
                int start = scrollPosOne;
                int end = Math.min(scrollPosOne + maxElements, materials.size());
                if (end < materials.size() - 1) {
                    drawContext.drawString(Minecraft.getInstance().font, Component.translatable("miapi.ui.material_detail.lower.scroll"), getX() + 3, currentY + 14 * (maxElements - 1), moreEntryColor, false);
                    end--;
                }
                if (start != 0) {
                    drawContext.drawString(Minecraft.getInstance().font, Component.translatable("miapi.ui.material_detail.higher.scroll"), getX() + 3, currentY, moreEntryColor, false);
                    start++;
                    currentY += 14;
                }
                for (int i = start; i < end; i++) {
                    int color = i == selected + scrollPosOne ? selectedColor : unselectedColor;
                    Component translation = getTranslation(materialKeys.get(i));
                    drawContext.drawString(Minecraft.getInstance().font, translation, getX() + 3, currentY, color, false);
                    currentY += 14;
                }
            }
            currentY = this.getY() + 3;
            if (materialList.size() > 1) {
                scrollPosTwo = Math.max(0, Math.min(scrollPosTwo, materialList.size() - 1));
                while (start > scrollPosTwo - 1) {
                    start--;
                }
                start = Math.max(0, start);
                end = Math.min(start + maxElements, materialList.size());
                while (end < scrollPosTwo + 2 && end < materialList.size()) {
                    start++;
                    end = Math.min(start + maxElements, materialList.size());
                }
                for (int i = start; i < end; i++) {
                    if (start > 0 && i == start) {
                        drawContext.drawString(
                                Minecraft.getInstance().font,
                                Component.translatable("miapi.ui.material_detail.higher"),
                                getX() + sizeBaseList + 6, currentY, moreEntryColor,
                                false);
                    } else if (i == end - 1 && end < materialList.size() - 1) {
                        drawContext.drawString(
                                Minecraft.getInstance().font,
                                Component.translatable("miapi.ui.material_detail.lower"),
                                getX() + sizeBaseList + 6, currentY,
                                moreEntryColor,
                                false);
                    } else {
                        int color = (i == scrollPosTwo) ? selectedColor : unselectedColor;
                        drawContext.drawString(
                                Minecraft.getInstance().font,
                                materialList.get(i).getTranslation(),
                                getX() + sizeBaseList + 6, currentY, color,
                                false);
                    }
                    currentY += 14;
                }
                //selectedMaterialUpdate(materialList.get(scrollPosOne));
                previewMaterial = materialList.get(scrollPosTwo);

            } else {
                previewMaterial = materialList.getFirst();
                //selectedMaterialUpdate(materialList.get(0));
            }
            RenderSystem.enableDepthTest();
        } else {
            lastRendered = false;
            scrollPosOne = 0;
            scrollPosTwo = 0;
            selected = 0;
        }
    }

    public static void selectedMaterialUpdate(Material material) {
        if (material != null && PreviewManager.currentPreviewMaterial != material) {
            ItemStack materialStack = new ItemStack(RegistryInventory.modularItem);
            materialStack.set(CompositeMaterial.COMPOSITE_MATERIAL_COMPONENT,
                    CompositeMaterial.getFromComposites(List.of(new MaterialOverwriteComposite(material), new AnyIngredientComposite())));
            PreviewManager.setCursorItemstack(materialStack);
            Material material1 = MaterialProperty.getMaterialFromIngredient(materialStack);
            Miapi.LOGGER.info(" " + material1);
        } else {
            ItemStack materialStack = new ItemStack(RegistryInventory.modularItem);
            PreviewManager.setCursorItemstack(materialStack);
            PreviewManager.resetCursorStack();
        }
    }

    public static Component getTranslation(String materialOrGroupKey) {
        if (MaterialProperty.materials.containsKey(materialOrGroupKey)) {
            Material material = MaterialProperty.materials.get(materialOrGroupKey);
            return material.getTranslation();
        }
        Component testTranslation = Component.translatable("miapi.material_group." + materialOrGroupKey);
        if (testTranslation.getString().equals("miapi.material_group." + materialOrGroupKey)) {
            return Component.literal(materialOrGroupKey);
        }
        return testTranslation;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            if (Screen.hasShiftDown() || Screen.hasControlDown()) {
                if (scrollY < 0) {
                    scrollPosTwo++;
                } else {
                    scrollPosTwo--;
                }
                return true;
            } else {
                scrollPosTwo = 0;
                start = 0;
                if (scrollY < 0) {
                    int maxElementsTotal = materialKeys.size();
                    if (selected + scrollPosOne == maxElementsTotal - 1) {
                    } else if (scrollPosOne == 0 && maxElements == maxElementsTotal) {
                        selected = Math.min(selected + 1, maxElements - 1);
                    } else if (scrollPosOne + maxElements == maxElementsTotal - 1) {
                        selected = Math.min(selected + 1, maxElements - 1);
                    } else if (selected == maxElements - 2) {
                        scrollPosOne++;
                    } else {
                        selected = Math.min(selected + 1, maxElements - 1);
                    }
                } else {
                    if (selected == 1 && scrollPosOne > 0) {
                        scrollPosOne--;
                    } else {
                        selected = Math.max(selected - 1, 0);
                    }
                }
                return true;
            }
        }
        return false;
    }
}
