package smartin.miapi.client.gui.crafting.statdisplay.material;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;

public class MaterialGroupTitleWidget extends InteractAbleWidget {
    ScrollingTextWidget scrollingTextWidget;
    Component hoverDescription;
    boolean hasHoverdescription;

    public MaterialGroupTitleWidget(int x, int y, int width, String materialKey) {
        super(x, y, width, 12, Component.literal(materialKey));
        scrollingTextWidget = new ScrollingTextWidget(x, y + 2, width, Component.translatableWithFallback("miapi.material_property.category." + materialKey, materialKey));
        scrollingTextWidget.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
        hoverDescription = Component.translatableWithFallback("miapi.material_property.category." + materialKey + ".description", materialKey);
        hasHoverdescription = !hoverDescription.getString().equals(materialKey);
        this.addChild(scrollingTextWidget);
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderHover(drawContext, mouseX, mouseY, delta);
        if (hasHoverdescription && isMouseOver(mouseX, mouseY)) {
            drawContext.renderTooltip(Minecraft.getInstance().font, hoverDescription, mouseX, mouseY);
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        scrollingTextWidget.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        scrollingTextWidget.setY(y + 2);

    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        scrollingTextWidget.setWidth(width);
    }
}
