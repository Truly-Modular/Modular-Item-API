package smartin.miapi.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

public class ClickAbleTextWidget extends TextFieldWidget {
    public ClickAbleTextWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height)) {
            setFocused(true);
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen) {
                screen.setFocused(this);
                screen.focusOn(this);
                Miapi.LOGGER.warn("setfocused");
            }
        } else {
            setFocused(false);
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen) {
                screen.setFocused(null);
                Miapi.LOGGER.warn("unfocused");
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(isActive() && isFocused()){
            super.keyPressed(keyCode,scanCode,modifiers);
            return true;
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }
}
