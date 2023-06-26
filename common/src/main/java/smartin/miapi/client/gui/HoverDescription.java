package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;

public class HoverDescription extends InteractAbleWidget {
    public Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/stat_display/hover_background.png");
    public MultiLineTextWidget textWidget;

    public HoverDescription(int x, int y, int width, int height, Text text) {
        super(x, y, width, height, Text.empty());
        textWidget = new MultiLineTextWidget(x, y + 1, width, height, text);
        this.addChild(textWidget);
    }

    public void setText(Text text) {
        textWidget.setText(text);
        this.width = textWidget.getWidth() + 5;
        this.height = textWidget.getHeight() + 5;
    }

    @Override
    public void renderHover(DrawContext context, int mouseX, int mouseY, float delta) {
        if (textWidget.rawText != null && !textWidget.rawText.getString().isEmpty()) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            drawTextureWithEdge(context, texture, this.getX(), this.getY(), this.width, this.height, 120, 32, 3);
            textWidget.setX(this.getX() + 3);
            textWidget.setY(this.getY() + 3);
            super.render(context, mouseX, mouseY, delta);
            RenderSystem.enableDepthTest();
        }
    }
}
