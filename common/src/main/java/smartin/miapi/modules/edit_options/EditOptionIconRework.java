package smartin.miapi.modules.edit_options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditOptionIconRework extends InteractAbleWidget {
    EditOption editOption;
    EditOptionRender editOptionRender;
    Consumer<EditOption> select;
    Supplier<EditOption> selected;

    public EditOptionIconRework(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> selected, EditOption option) {
        this(x, y, width, height, select, selected, option, option);
    }

    public EditOptionIconRework(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> selected, EditOption option, EditOptionRender render) {
        super(x, y, width, height, Component.empty());
        this.select = select;
        this.editOption = option;
        this.selected = selected;
        this.editOptionRender = render;
    }

    @Override
    public boolean isHoveredOrFocused() {
        return editOption == selected.get();
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderWidget(drawContext, mouseX, mouseY, delta);
        drawContext.blit(CraftingScreen.BACKGROUND_TEXTURE, getX() - 1, getY(), 338 + (isHoveredOrFocused() ? 33 : 0), 25, 33, 28, 512, 512);
        editOptionRender.renderIcon(drawContext, isHoveredOrFocused(), getX() + 8 + (isHoveredOrFocused() ? -2 : 0), getY() + 5, delta);
        if (isMouseOver(mouseX, mouseY) && !editOptionRender.getHoverName().getString().isBlank()) {
            drawContext.renderTooltip(Minecraft.getInstance().font, editOptionRender.getHoverName(), mouseX, mouseY);
        }
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
