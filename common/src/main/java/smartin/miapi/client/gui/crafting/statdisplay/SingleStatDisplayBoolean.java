package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.ScrollingTextWidget;

@Environment(EnvType.CLIENT)
public abstract class SingleStatDisplayBoolean extends SingleStatDisplayDouble {

    protected SingleStatDisplayBoolean(int x, int y, int width, int height, StatListWidget.TextGetter title, StatListWidget.TextGetter hover) {
        super(x, y, width, height, title, hover);
        this.minValue = 0;
        this.maxValue = 1;
    }

    @Override
    public double getValue(ItemStack stack) {
        return getValueItemStack(stack) ? 1 : 0;
    }

    public int getWidthDesired() {
        int textWidth = Minecraft.getInstance().font.width(this.text.resolve(original).getString());
        int numberWidth = Minecraft.getInstance().font.width(getText(compareToValue).getString());
        int size = 1;
        if (textWidth + numberWidth > 76 - 6) {
            size = 2;
        }
        return 76 * size;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        return hasValueItemStack(original) || hasValueItemStack(compareTo);
    }

    public abstract boolean getValueItemStack(ItemStack itemStack);

    public abstract boolean hasValueItemStack(ItemStack itemStack);

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        //double oldValue = getInt(original);
        //double compareToValue = getInt(compareTo);

        double min = Math.min(minValue, Math.min(oldValue, compareToValue));
        double max = Math.max(maxValue, Math.max(oldValue, compareToValue));

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 339, 6, 51, 19, width, height, 512, 512, 2);

        textWidget.setX(this.getX() + 3);
        textWidget.setY(this.getY() + 3);
        textWidget.setWidth(this.width - 25);

        statBar.setX(this.getX() + 2);
        statBar.setY(this.getY() + 15);
        statBar.setWidth(this.width - 4);
        statBar.setHeight(1);
        if (oldValue < compareToValue) {
            statBar.setPrimary((oldValue - min) / (max - min), FastColor.ARGB32.color(255, 255, 255, 255));
            statBar.setSecondary((compareToValue - min) / (max - min), getGreen());
            compareValue.textColor = getGreen();
        } else {
            statBar.setPrimary((compareToValue - min) / (max - min), FastColor.ARGB32.color(255, 255, 255, 255));
            statBar.setSecondary((oldValue - min) / (max - min), getRed());
            compareValue.textColor = getRed();
        }
        if (oldValue == compareToValue) {
            currentValue.setX(this.getX() - 3);
            currentValue.setY(this.getY() + 5);
            currentValue.setWidth(this.getWidth());
            currentValue.setText(Component.literal(getText(oldValue).getString() + postfix.getString()));
            currentValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            currentValue.render(drawContext, mouseX, mouseY, delta);
        } else {
            compareValue.setX(this.getX() - 3);
            compareValue.setY(this.getY() + 5);
            compareValue.setWidth(this.getWidth());
            compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            compareValue.setText(Component.literal(getText(compareToValue).getString() + postfix.getString()));
            compareValue.render(drawContext, mouseX, mouseY, delta);
        }
        statBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    public static Component getText(double value) {
        return getText(value > 0);
    }

    public static Component getText(boolean value) {
        if (value) {
            return Component.translatable("miapi.ui.boolean.true");
        } else {
            return Component.translatable("miapi.ui.boolean.false");
        }
    }
}
