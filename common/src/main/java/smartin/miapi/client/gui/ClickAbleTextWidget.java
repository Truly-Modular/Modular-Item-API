package smartin.miapi.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * This Widget is an Extention of the {@link TextFieldWidget}
 * It auto-disables the e-close mechanic and is selectable by clicking
 */
public class ClickAbleTextWidget extends TextFieldWidget {
    public ClickAbleTextWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
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
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen) {
                screen.setFocused(this);
            }
        } else {
            setFocused(false);
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen) {
                screen.setFocused(null);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isActive() && isFocused()) {
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
