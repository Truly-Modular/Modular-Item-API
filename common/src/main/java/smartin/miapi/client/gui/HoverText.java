package smartin.miapi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class HoverText extends InteractAbleWidget{

    Component hoverDescription;

    public HoverText(int x, int y, int width, int height, Component title) {
        super(x, y, width, height, title);
        hoverDescription = title;
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderHover(drawContext, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY)) {
            drawContext.renderTooltip(Minecraft.getInstance().font, hoverDescription, mouseX, mouseY);
        }
    }
}
