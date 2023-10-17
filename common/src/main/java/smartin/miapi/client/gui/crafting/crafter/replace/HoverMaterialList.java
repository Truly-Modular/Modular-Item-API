package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.AllowedMaterial;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HoverMaterialList extends InteractAbleWidget {
    public LinkedHashMap<String, List<Material>> materials = new LinkedHashMap<>();
    public List<String> materialKeys = new ArrayList<>();
    public int selected = 0;
    public int scrollPosOne = 0;
    public int scrollPosTwo = 0;

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
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            boolean subMaterials = false;
            int currentY = this.getY() + 3;
            String selectedMaterialOrGroup = materialKeys.get(selected);
            List<Material> materialList = materials.get(selectedMaterialOrGroup);
            int sizeBaseList = 30;
            for (int i = 0; i < Math.min(materials.size(), 8); i++) {
                Text material = getTranslation(materialKeys.get(i));
                sizeBaseList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(material), sizeBaseList);
            }
            int sizeDetailList = 0;
            if(materialList.size()>1){
                for (Material m : materialList) {
                    Text material = getTranslation(m.getKey());
                    sizeDetailList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(material), sizeDetailList);
                }
            }
            drawContext.fill(getX(), getY(), getX() + sizeDetailList + sizeBaseList + 10, getY() + materials.size() * 14, ColorHelper.Argb.getArgb(210, 0, 0, 0));
            for (int i = 0; i < Math.min(materials.size(), 8); i++) {
                int color = ColorHelper.Argb.getArgb(255, 200, 200, 200);
                if (i == selected) {
                    color = ColorHelper.Argb.getArgb(255, 255, 255, 255);
                }
                Text translation = getTranslation(materialKeys.get(i));
                drawContext.drawText(MinecraftClient.getInstance().textRenderer, translation, getX() + 3, currentY, color, false);
                currentY += 14;
            }
            currentY = this.getY() + 3;
            if (materialList.size() > 1) {
                for (int i = 0; i < Math.min(materialList.size(), 8); i++) {
                    int color = ColorHelper.Argb.getArgb(255, 200, 200, 200);
                    Text material = getTranslation(materialList.get(i).getKey());
                    drawContext.drawText(MinecraftClient.getInstance().textRenderer, material, getX() + sizeBaseList + 6, currentY, color, false);
                    currentY += 14;
                }
            }
            RenderSystem.enableDepthTest();
        }
        else{
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
            if (amount < 0) {
                selected = Math.min(selected + 1, materials.size() - 1);
            } else {
                selected = Math.max(selected - 1, 0);
            }
            return true;
        }
        return false;
    }
}
