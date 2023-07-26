package smartin.miapi.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class SimpleTextWidget extends InteractAbleWidget {
    public Text text;
    public boolean hasShadow = true;
    public TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public SimpleTextWidget(int x, int y, int width, int height, Text text) {
        super(x, y, width, height, Text.empty());
        this.text = text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(textRenderer, text, getX(), getY(), -1, hasShadow);
    }
}
