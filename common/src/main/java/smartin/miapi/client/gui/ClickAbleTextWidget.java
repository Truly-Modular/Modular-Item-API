package smartin.miapi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

/**
 * This Widget is an Extention of the {@link EditBox}
 * It auto-disables the e-close mechanic and is selectable by clicking
 */
public class ClickAbleTextWidget extends EditBox {
    public ClickAbleTextWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.getX() && mouseX < (this.getX() + this.width) && mouseY >= this.getY() && mouseY < (this.getY() + this.height)) {
            setFocused(true);
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
                screen.setFocused(this);
            }
        } else {
            setFocused(false);
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
                screen.setFocused(null);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canConsumeInput() && isFocused()) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
