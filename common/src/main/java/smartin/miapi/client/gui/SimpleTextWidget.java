package smartin.miapi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SimpleTextWidget extends InteractAbleWidget {
    public Component text;
    public boolean hasShadow = true;
    public Font textRenderer = Minecraft.getInstance().font;

    public SimpleTextWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, Component.empty());
        this.text = text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.drawString(textRenderer, text, getX(), getY(), -1, hasShadow);
    }
}
