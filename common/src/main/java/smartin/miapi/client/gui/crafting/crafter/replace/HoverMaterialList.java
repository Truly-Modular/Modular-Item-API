package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.AllowedMaterial;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

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
    final int selectedColor = ColorHelper.Argb.getArgb(255, 255, 255, 255);
    final int unselectedColor = ColorHelper.Argb.getArgb(255, 200, 200, 200);
    final int moreEntryColor = ColorHelper.Argb.getArgb(255, 160, 160, 160);

    public HoverMaterialList(ItemModule module, int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        materialKeys = AllowedMaterial.property.getAllowedKeys(module);
        for (String key : materialKeys) {
            materials.put(key, AllowedMaterial.property.getMaterials(key));
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 404, 96, 20, 11, 512, 512);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            int currentY = this.getY() + 3;
            String selectedMaterialOrGroup = materialKeys.get(selected + scrollPosOne);
            List<Material> materialList = materials.get(selectedMaterialOrGroup);
            int sizeBaseList = 30;
            int verticalSize = Math.min(materials.size(), maxElements);
            verticalSize = Math.max(Math.min(materialList.size(), maxElements), verticalSize);
            for (int i = scrollPosOne; i < Math.min(materials.size(), maxElements + scrollPosOne); i++) {
                Text material = getTranslation(materialKeys.get(i));
                sizeBaseList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(material), sizeBaseList);
            }
            int sizeDetailList = 0;
            if (materialList.size() > 1) {
                for (Material m : materialList) {
                    Text material = getTranslation(m.getKey());
                    sizeDetailList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(material), sizeDetailList);
                }
            }
            drawContext.fill(getX(), getY(), getX() + sizeDetailList + sizeBaseList + 10, getY() + verticalSize * 14, ColorHelper.Argb.getArgb(210, 0, 0, 0));
            if (materials.size() > 1) {
                scrollPosOne = Math.max(0, Math.min(materials.size() - maxElements - 1, scrollPosOne));
                int start = scrollPosOne;
                int end = Math.min(scrollPosOne + maxElements, materials.size());
                if (end < materials.size() - 1) {
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, "...(scroll)", getX() + 3, currentY + 14 * (maxElements - 1), moreEntryColor, false);
                    end--;
                }
                if (start != 0) {
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, "...", getX() + 3, currentY, moreEntryColor, false);
                    start++;
                    currentY += 14;
                }
                for (int i = start; i < end; i++) {
                    int color = i == selected + scrollPosOne ? selectedColor : unselectedColor;
                    Text translation = getTranslation(materialKeys.get(i));
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, translation, getX() + 3, currentY, color, false);
                    currentY += 14;
                }
            }
            currentY = this.getY() + 3;
            if (materialList.size() > 1) {
                scrollPosTwo = Math.max(0, Math.min(materialList.size() - maxElements - 1, scrollPosTwo));
                int start = scrollPosTwo;
                int end = Math.min(scrollPosTwo + maxElements, materialList.size() - 1);
                if (end < materialList.size() - 1) {
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, "...(shift)", getX() + sizeBaseList + 6, currentY + 14 * (maxElements - 1), moreEntryColor, false);
                    end--;
                }
                if (start != 0) {
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, "...", getX() + sizeBaseList + 6, currentY, moreEntryColor, false);
                    start++;
                    currentY += 14;
                }
                for (int i = start; i < end; i++) {
                    Text material = getTranslation(materialList.get(i).getKey());
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, material, getX() + sizeBaseList + 6, currentY, unselectedColor, false);
                    currentY += 14;
                }
            }
            RenderSystem.enableDepthTest();
        } else {
            scrollPosOne = 0;
            scrollPosTwo = 0;
        }
    }

    public Text getTranslation(String materialOrGroupKey) {
        if (MaterialProperty.materials.containsKey(materialOrGroupKey)) {
            Material material = MaterialProperty.materials.get(materialOrGroupKey);
            return Text.translatable(material.getData("translation"));
        }
        Text testTranslation = Text.translatable("miapi.material_group." + materialOrGroupKey);
        if (testTranslation.getString().equals("miapi.material_group." + materialOrGroupKey)) {
            return Text.literal(materialOrGroupKey);
        }
        return testTranslation;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY)) {
            if (Screen.hasShiftDown() || Screen.hasControlDown()) {
                if (amount < 0) {
                    scrollPosTwo++;
                } else {
                    scrollPosTwo--;
                }
                return true;
            } else {
                if (amount < 0) {
                    int maxElementsTotal = materialKeys.size();
                    if (selected + scrollPosOne == maxElementsTotal - 1) {
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
