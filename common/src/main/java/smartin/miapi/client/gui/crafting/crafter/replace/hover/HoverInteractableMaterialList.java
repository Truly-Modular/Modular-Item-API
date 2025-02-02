package smartin.miapi.client.gui.crafting.crafter.replace.hover;

import net.minecraft.client.gui.DrawContext;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.ItemModule;

public class HoverInteractableMaterialList extends HoverMaterialList {

    public HoverInteractableMaterialList(ItemModule module, int x, int y, int width, int height) {
        super(module, x, y, width, height);
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
            if (CraftingScreen.getInstance() != null && isMouseOver(mouseX, mouseY)) {
                CraftingScreen.getInstance().overwriteMouseY = -1000;
                CraftingScreen.getInstance().overwriteMouseX = -1000;
            }
        }
        super.render(drawContext, mouseX, mouseY, delta);
    }

    public boolean isMouseOver(double mouseX, double mouseY, int xStart, int yStart, int xWidth, int yHeight) {
        return this.active && this.visible && mouseX >= (double) xStart && mouseY >= (double) yStart && mouseX < (double) (this.getX() + xWidth) && mouseY < (double) (this.getY() + yHeight);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (lastRendered || permaOpen) {
            return isMouseOver(mouseX, mouseY, getX(), getY(), sizeBaseList + sizeDetailList, verticalSize);
        } else {
            return super.isMouseOver(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY, getX(), getY(), sizeBaseList + sizeDetailList, verticalSize)) {
            if (mouseX > getX() + sizeBaseList) {
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            this.permaOpen = !permaOpen;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
