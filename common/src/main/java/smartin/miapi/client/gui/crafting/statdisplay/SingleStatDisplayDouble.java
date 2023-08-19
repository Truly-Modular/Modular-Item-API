package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public abstract class SingleStatDisplayDouble extends InteractAbleWidget implements SingleStatDisplay, Drawable {
    public Identifier texture = new Identifier("textures/gui/container/inventory.png");
    public ItemStack original = ItemStack.EMPTY;
    public ItemStack compareTo = ItemStack.EMPTY;
    public StatBar statBar;
    public ScrollingTextWidget currentValue;
    public ScrollingTextWidget compareValue;
    public ScrollingTextWidget centerValue;
    public ScrollingTextWidget textWidget;
    public double maxValue = 100;
    public double minValue = 0;
    public DecimalFormat modifierFormat;
    public StatDisplay.TextGetter text;
    public StatDisplay.TextGetter hover;
    public HoverDescription hoverDescription;

    protected SingleStatDisplayDouble(int x, int y, int width, int height, StatDisplay.TextGetter title, StatDisplay.TextGetter hover) {
        super(x, y, width, height, Text.empty());
        text = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(x, y, 80, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(x, y, 50, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(x, y, 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
        compareValue = new ScrollingTextWidget(x, y, 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
        statBar = new StatBar(0, 0, width, 10, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(x, y, List.of());
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        textWidget.setText(text.resolve(mainStack));
        hoverDescription.setText(hover.resolve(mainStack));
        this.original = original;
        this.compareTo = compareTo;
        return true;
    }

    public abstract double getValue(ItemStack stack);

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        double oldValue = getValue(original);
        double compareToValue = getValue(compareTo);

        double min = Math.min(minValue,Math.min(oldValue,compareToValue));
        double max = Math.max(maxValue,Math.max(oldValue,compareToValue));

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 0, 166, 120, 32, width, height, 256, 256, 2);

        textWidget.setX(this.getX() + 5);
        textWidget.setY(this.getY() + 5);
        textWidget.setWidth(this.width - 8);

        statBar.setX(this.getX() + 5);
        statBar.setY(this.getY() + 25);
        statBar.setWidth(this.width - 10);
        statBar.setHeight(3);
        String centerText = "";
        if (oldValue < compareToValue) {
            statBar.setPrimary((oldValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((compareToValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 0, 255, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 0, 255, 0);
        } else {
            statBar.setPrimary((compareToValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((oldValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 0, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 255, 0, 0);
        }
        if (oldValue == compareToValue) {
            compareValue.textColor = ColorHelper.Argb.getArgb(0, 255, 0, 0);
        } else {
            compareValue.setX(this.getX() + 5);
            compareValue.setY(this.getY() + 15);
            compareValue.setText(Text.of(modifierFormat.format(compareToValue)));
            compareValue.render(drawContext, mouseX, mouseY, delta);
            centerText = "→";
        }
        currentValue.setX(this.getX() + 5);
        currentValue.setY(this.getY() + 15);
        currentValue.setText(Text.literal(modifierFormat.format(oldValue)));
        currentValue.render(drawContext, mouseX, mouseY, delta);
        centerValue.setX(this.getX() + 5);
        centerValue.setY(this.getY() + 15);
        centerValue.setText(Text.literal(centerText));
        centerValue.render(drawContext, mouseX, mouseY, delta);
        statBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    public InteractAbleWidget getHoverWidget() {
        return hoverDescription;
    }
}
