package smartin.miapi.modules.edit_options;

import smartin.miapi.client.gui.InteractAbleWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EditOptionIcon extends InteractAbleWidget {
    ResourceLocation texture;
    EditOption editOption;
    Consumer<EditOption> select;
    Supplier<EditOption> getSelected;
    int u;
    int v;
    int textureWidth;
    int textureHeight;
    String langKey = "";

    public EditOptionIcon(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected, ResourceLocation textureIdentifier, int u, int v, int textureHeight, int textureWidth, EditOption option) {
        this(x, y, width, height, select, getSelected, textureIdentifier, u, v, textureHeight, textureWidth, "", option);
    }

    public EditOptionIcon(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected, ResourceLocation textureIdentifier, int u, int v, int textureHeight, int textureWidth, String langKey, EditOption option) {
        super(x, y, width, height, Component.empty());
        this.select = select;
        this.getSelected = getSelected;
        this.u = u;
        this.v = v;
        this.textureHeight = textureHeight;
        this.textureWidth = textureWidth;
        texture = textureIdentifier;
        editOption = option;
        this.langKey = langKey;
    }

    public boolean isHoveredOrFocused() {
        return editOption == getSelected.get();
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        float vOffset = 0;
        if (isHoveredOrFocused()) {
            vOffset = this.getHeight();
        }
        drawContext.blit(texture, getX(), getY(), (float) u, vOffset + v, getWidth(), getHeight(), textureWidth, textureHeight);
        if (!langKey.equals("")) {
            if (isMouseOver(mouseX, mouseY)) {
                drawContext.renderTooltip(Minecraft.getInstance().font, Component.translatable(langKey), mouseX, mouseY);
            }
        }
        //drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable(langKey), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && !isHoveredOrFocused()) {
            select.accept(editOption);
            return true;
        }
        return false;
    }
}
