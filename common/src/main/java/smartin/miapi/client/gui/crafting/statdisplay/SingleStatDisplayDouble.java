package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.StatBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    public DecimalFormat modifierFormat;
    public Text text;
    public Text hover;
    public HoverDescription hoverDescription;

    public SingleStatDisplayDouble(int x, int y, int width, int height, Text title, Text hover) {
        super(x, y, width, height, Text.empty());
        text = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(x, y, 80, text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(x, y, 50, text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(x, y, (int) ((80 - 10) * (1.0 / 1.2)), text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.ORIENTATION.CENTERED);
        compareValue = new ScrollingTextWidget(x, y, (int) ((80 - 10) * (1.0 / 1.2)), text, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.ORIENTATION.RIGHT);
        statBar = new StatBar(0, 0, width, 10, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(x, y, width, height, hover);
    }

    public abstract double getValue(ItemStack stack);

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        double oldValue = getValue(original);
        double compareToValue = getValue(compareTo);
        double higher = Math.max(oldValue, compareToValue) * 1.2;
        if (higher == 0) {
            higher = 1;
        }

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
            statBar.setPrimary(oldValue / higher, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary(compareToValue / higher, ColorHelper.Argb.getArgb(255, 0, 255, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 0, 255, 0);
        } else {
            statBar.setPrimary(compareToValue / higher, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary(oldValue / higher, ColorHelper.Argb.getArgb(255, 255, 0, 0));
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
        centerValue.setY(this.getY()+15);
        centerValue.setText(Text.literal(centerText));
        centerValue.render(drawContext, mouseX, mouseY, delta);
        statBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    public InteractAbleWidget getHoverWidget() {
        return hoverDescription;
    }

    public class HoverDescription extends InteractAbleWidget {
        public Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/stat_display/hover_background.png");
        public MultiLineTextWidget textWidget;
        public Text text;

        public HoverDescription(int x, int y, int width, int height, Text text) {
            super(x, y, width, height, Text.empty());
            textWidget = new MultiLineTextWidget(x, y + 1, width, height, text);
            this.addChild(textWidget);
            this.width = textWidget.getWidth() + 5;
            this.height = textWidget.getHeight() + 5;
            this.text = text;
        }

        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            if (!text.getString().isEmpty()) {
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                drawTextureWithEdge(drawContext,texture, this.getX(), this.getY(), this.width, this.height, 120, 32, 3);
                textWidget.setX(this.getX()+3);
                textWidget.setY(this.getY()+3);
                super.render(drawContext, mouseX, mouseY, delta);
                RenderSystem.enableDepthTest();
            }
        }
    }
}
