package smartin.miapi.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HoverText extends InteractAbleWidget{

    Text hoverDescription;

    public HoverText(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
        hoverDescription = title;
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.renderHover(drawContext, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY)) {
            drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, hoverDescription, mouseX, mouseY);
        }
    }
}
