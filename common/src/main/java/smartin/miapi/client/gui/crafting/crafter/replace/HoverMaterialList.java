package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;

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
    final int maxElements = 8;
    final int selectedColor = FastColor.ARGB32.color(255, 255, 255, 255);
    final int unselectedColor = FastColor.ARGB32.color(255, 200, 200, 200);
    final int moreEntryColor = FastColor.ARGB32.color(255, 160, 160, 160);

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
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            int currentY = this.getY() + 3;
            if(materialKeys.isEmpty()){
                return;
            }
            String selectedMaterialOrGroup = materialKeys.get(selected + scrollPosOne);
            List<Material> materialList = materials.get(selectedMaterialOrGroup);
            int sizeBaseList = 30;
            int verticalSize = Math.min(materials.size(), maxElements);
            verticalSize = Math.clamp(materialList.size(), verticalSize, maxElements);
            for (int i = scrollPosOne; i < Math.min(materials.size(), maxElements + scrollPosOne); i++) {
                Component material = getTranslation(materialKeys.get(i));
                sizeBaseList = Math.max(Minecraft.getInstance().font.width(material), sizeBaseList);
            }
            int sizeDetailList = 0;
            if (materialList.size() > 1) {
                for (Material m : materialList) {
                    Component material = m.getTranslation();
                    sizeDetailList = Math.max(Minecraft.getInstance().font.width(material), sizeDetailList);
                }
            }
            drawContext.fill(getX(), getY(), getX() + sizeDetailList + sizeBaseList + 10, getY() + verticalSize * 14, FastColor.ARGB32.color(210, 0, 0, 0));
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
                scrollPosTwo = Math.max(0, Math.min(materialList.size() - maxElements - 1, scrollPosTwo));
                int start = scrollPosTwo;
                int end = Math.min(scrollPosTwo + maxElements - 1, materialList.size() - 1);
                if (end < materialList.size() - 2) {
                    drawContext.drawString(Minecraft.getInstance().font, Component.translatable("miapi.ui.material_detail.lower"), getX() + sizeBaseList + 6, currentY + 14 * (maxElements - 1), moreEntryColor, false);
                    end--;
                }
                if (start != 0) {
                    drawContext.drawString(Minecraft.getInstance().font, Component.translatable("miapi.ui.material_detail.higher"), getX() + sizeBaseList + 6, currentY, moreEntryColor, false);
                    start++;
                    currentY += 14;
                }
                for (int i = start; i <= end; i++) {
                    Component material = materialList.get(i).getTranslation();
                    drawContext.drawString(Minecraft.getInstance().font, material, getX() + sizeBaseList + 6, currentY, unselectedColor, false);
                    currentY += 14;
                }
            }
            RenderSystem.enableDepthTest();
        } else {
            scrollPosOne = 0;
            scrollPosTwo = 0;
            selected = 0;
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
