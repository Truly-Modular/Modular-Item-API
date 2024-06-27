package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import smartin.miapi.client.gui.crafting.CraftingScreen;

/**
 * A complex utility to render a bar with 3 parts in different colors
 */
@Environment(EnvType.CLIENT)
public class StatBar extends InteractAbleWidget {
    public ResourceLocation texture = CraftingScreen.INVENTORY_LOCATION;

    double primaryPercent = 0;
    double secondaryPercent = 0;
    int primaryColor = 0;
    int secondaryColor = 0;
    int offColor;
    int shadowSize = 1;

    public StatBar(int x, int y, int width, int height, int offColor) {
        super(x, y, width, height, Component.empty());
        this.offColor = offColor;
    }

    public void setPrimary(double primaryPercent, int color) {
        primaryPercent = Math.min(1, Math.max(0, primaryPercent));
        this.primaryColor = color;
        this.primaryPercent = primaryPercent;
    }

    public void setSecondary(double secondaryPercent, int color) {
        secondaryPercent = Math.min(1, Math.max(0, secondaryPercent));
        this.secondaryColor = color;
        this.secondaryPercent = secondaryPercent;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, FastColor.ARGB32.color(255, 255, 0, 255));
        context.fill(getX(), getY(), (int) (getX() + width * primaryPercent), height + getY(), primaryColor);
        context.fill((int) (getX() + width * primaryPercent), getY(), (int) (getX() + width * secondaryPercent), height + getY(), secondaryColor);
        context.fill((int) (getX() + width * secondaryPercent), getY(), getX() + width, height + getY(), offColor);
        drawTextureWithEdge(context, texture, getX(), getY()+height, 339, 4, 7, 1, getWidth(), shadowSize, 512, 512, 1);
    }
}
