package smartin.miapi.client.gui.crafting.crafter.replace.hover;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.AllowedMaterial;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.material.NBTMaterial;
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
    protected final int selectedColor = ColorHelper.Argb.getArgb(255, 255, 255, 255);
    protected final int unselectedColor = ColorHelper.Argb.getArgb(255, 200, 200, 200);
    protected final int moreEntryColor = ColorHelper.Argb.getArgb(255, 160, 160, 160);
    protected int start = 0;
    protected int end = 0;
    protected boolean lastRendered = false;
    protected Material previewMaterial = null;
    protected int realMouseX = 0;
    protected int realMouseY = 0;
    protected int sizeDetailList, verticalSize, sizeBaseList = 0;
    protected boolean permaOpen = false;


    public HoverMaterialList(ItemModule module, int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        materialKeys = AllowedMaterial.property.getAllowedKeys(module);
        for (String key : materialKeys) {
            materials.put(key, AllowedMaterial.property.getMaterials(key)
                    .stream()
                    .sorted(Comparator.comparing(m -> m.getTranslation().getString()))
                    .toList());
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawTexture(
                CraftingScreen.BACKGROUND_TEXTURE,
                getX(), getY(),
                404, 96,
                20, 11,
                512, 512);
        realMouseX = mouseX;
        realMouseY = mouseY;
        if (lastRendered || permaOpen) {
            selectedMaterialUpdate(previewMaterial);
            if (CraftingScreen.getInstance() != null) {
                //CraftingScreen.getInstance().overwriteMouseY = -1000;
                //CraftingScreen.getInstance().overwriteMouseX = -1000;
            }
        }
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int currentY = this.getY() + 3;
        String selectedMaterialOrGroup = materialKeys.get(selected + scrollPosOne);
        List<Material> materialList = materials.get(selectedMaterialOrGroup);
        sizeBaseList = 30;
        verticalSize = Math.min(materials.size(), maxElements);
        verticalSize = Math.max(Math.min(materialList.size(), maxElements), verticalSize);
        verticalSize *= 14;

        for (int i = scrollPosOne; i < Math.min(materials.size(), maxElements + scrollPosOne); i++) {
            sizeBaseList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(getTranslation(materialKeys.get(i))), sizeBaseList);
        }

        sizeDetailList = 0;
        for (Material m : materialList) {
            sizeDetailList = Math.max(MinecraftClient.getInstance().textRenderer.getWidth(getTranslation(m.getKey())), sizeDetailList);
        }
        sizeDetailList += 10;
        if (isMouseOver(realMouseX, realMouseY) || permaOpen) {
            this.lastRendered = true;
            RenderSystem.disableDepthTest();

            drawContext.fill(
                    getX(), getY(),
                    getX() + sizeDetailList + sizeBaseList, getY() + verticalSize,
                    ColorHelper.Argb.getArgb(210, 0, 0, 0));

            if (!materials.isEmpty()) {
                scrollPosOne = Math.max(0, Math.min(materials.size() - maxElements, scrollPosOne));
                int startOne = scrollPosOne;
                int endOne = Math.min(scrollPosOne + maxElements, materials.size());

                if (startOne > 0) {
                    drawContext.drawText(
                            MinecraftClient.getInstance().textRenderer,
                            Text.translatable("miapi.ui.material_detail.higher.scroll"),
                            getX() + 3, currentY,
                            moreEntryColor,
                            false);
                    startOne++;
                    currentY += 14;
                }

                for (int i = startOne; i < endOne; i++) {
                    int color = (i == selected + scrollPosOne) ? selectedColor : unselectedColor;
                    drawContext.drawText(
                            MinecraftClient.getInstance().textRenderer,
                            getTranslation(materialKeys.get(i)), getX() + 3,
                            currentY,
                            color,
                            false);
                    currentY += 14;
                }

                if (endOne < materials.size()) {
                    drawContext.drawText(
                            MinecraftClient.getInstance().textRenderer,
                            Text.translatable("miapi.ui.material_detail.lower.scroll"),
                            getX() + 3, currentY, moreEntryColor,
                            false);
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
                        drawContext.drawText(
                                MinecraftClient.getInstance().textRenderer,
                                Text.translatable("miapi.ui.material_detail.higher"),
                                getX() + sizeBaseList + 6, currentY, moreEntryColor,
                                false);
                    } else if (i == end - 1 && end < materialList.size() - 1) {
                        drawContext.drawText(
                                MinecraftClient.getInstance().textRenderer,
                                Text.translatable("miapi.ui.material_detail.lower"),
                                getX() + sizeBaseList + 6, currentY,
                                moreEntryColor,
                                false);
                    } else {
                        int color = (i == scrollPosTwo) ? selectedColor : unselectedColor;
                        drawContext.drawText(
                                MinecraftClient.getInstance().textRenderer,
                                getTranslation(materialList.get(i).getKey()),
                                getX() + sizeBaseList + 6, currentY, color,
                                false);
                    }
                    currentY += 14;
                }
                //selectedMaterialUpdate(materialList.get(scrollPosOne));
                previewMaterial = materialList.get(scrollPosTwo);

            } else {
                previewMaterial = materialList.get(0);
                //selectedMaterialUpdate(materialList.get(0));
            }

            //Material selectedMaterial = materialList.get(Math.min(selected + scrollPosTwo, materialList.size() - 1));
            //selectedMaterialUpdate(selectedMaterial);

            RenderSystem.enableDepthTest();
        } else {
            if (lastRendered || permaOpen) {
                previewMaterial = null;
                selectedMaterialUpdate(null);
            }
            lastRendered = false;
            scrollPosOne = 0;
            scrollPosTwo = 0;
            selected = 0;
        }
    }

    public boolean isMouseOverFull(double mouseX, double mouseY, int otherWidth, int otherHeight) {
        return this.active && this.visible && mouseX >= (double) this.getX() && mouseY >= (double) this.getY() && mouseX < (double) (this.getX() + otherWidth) && mouseY < (double) (this.getY() + otherHeight);
    }

    public static void selectedMaterialUpdate(Material material) {
        if (material != null && PreviewManager.currentPreviewMaterial != material) {
            ItemStack materialStack = new ItemStack(RegistryInventory.modularItem);
            NBTMaterial.setMaterial(material, materialStack);
            PreviewManager.setCursorItemstack(materialStack);
        } else {
            ItemStack materialStack = new ItemStack(RegistryInventory.modularItem);
            PreviewManager.setCursorItemstack(materialStack);
            PreviewManager.resetCursorStack();
        }
    }


    public static Text getTranslation(String materialOrGroupKey) {
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
                scrollPosTwo = 0;
                start = 0;
                if (amount < 0) {
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
