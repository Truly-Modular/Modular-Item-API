package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.client.gui.HoverDescription;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.StatBar;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.config.MiapiConfig;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public abstract class SingleStatDisplayDouble extends InteractAbleWidget implements SingleStatDisplay, Drawable {
    public Identifier texture = CraftingScreen.BACKGROUND_TEXTURE;
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
    public StatListWidget.TextGetter text;
    public StatListWidget.TextGetter hover;
    public HoverDescription hoverDescription;
    public Text postfix = Text.of("");
    public boolean inverse = false;
    double oldValue = 0;
    double compareToValue = 0;

    protected SingleStatDisplayDouble(int x, int y, int width, int height, StatListWidget.TextGetter title, StatListWidget.TextGetter hover) {
        super(x, y, width, height, Text.empty());
        text = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(x, y, 80, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(x, y, 50, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(x, y, 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
        compareValue = new ScrollingTextWidget(x, y, 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
        statBar = new StatBar(0, 0, width, 1, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(x, y, List.of());
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public int getRed(){
        if(inverse){
            return MiapiConfig.INSTANCE.client.guiColors.green.argb();
        }
        else{
            return MiapiConfig.INSTANCE.client.guiColors.red.argb();
        }
    }

    public int getGreen(){
        if(inverse){
            return MiapiConfig.INSTANCE.client.guiColors.red.argb();
        }
        else{
            return MiapiConfig.INSTANCE.client.guiColors.green.argb();
        }
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        this.original = original;
        this.compareTo = compareTo;
        compareToValue = getValue(compareTo);
        oldValue = getValue(original);
        textWidget.setText(text.resolve(mainStack));
        hoverDescription.setText(hover.resolve(mainStack));
        compareValue.setText(Text.of(modifierFormat.format(compareToValue)));
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(this.text.resolve(original));
        int numberWidth = MinecraftClient.getInstance().textRenderer.getWidth(compareValue.getText());
        int size = Math.min(3, Math.max(1, ((textWidth + numberWidth) / 47)));
        this.setWidth(51 * size);
        return true;
    }

    public int getHeightDesired() {
        return 19;
    }

    public int getWidthDesired() {
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(this.text.resolve(original).getString());
        int numberWidth = MinecraftClient.getInstance().textRenderer.getWidth(compareValue.getText().getString());
        int size = 1;
        if (textWidth + numberWidth > 76 - 6) {
            size = 2;
        }
        return 76 * size;
    }

    public abstract double getValue(ItemStack stack);

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
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
            statBar.setPrimary((oldValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((compareToValue - min) / (max - min), getGreen());
            compareValue.textColor = getGreen();
        } else {
            statBar.setPrimary((compareToValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((oldValue - min) / (max - min), getRed());
            compareValue.textColor = getRed();
        }
        if (oldValue == compareToValue) {
            currentValue.setX(this.getX() - 3);
            currentValue.setY(this.getY() + 5);
            currentValue.setWidth(this.getWidth());
            currentValue.setText(Text.literal(modifierFormat.format(oldValue) + postfix.getString()));
            currentValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            currentValue.render(drawContext, mouseX, mouseY, delta);
        } else {
            compareValue.setX(this.getX() - 3);
            compareValue.setY(this.getY() + 5);
            compareValue.setWidth(this.getWidth());
            compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            compareValue.setText(Text.literal(Text.of(modifierFormat.format(compareToValue)).getString() + postfix.getString()));
            compareValue.render(drawContext, mouseX, mouseY, delta);
        }
        statBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            Text text1 = this.hover.resolve(compareTo);
            if (!text1.getString().isEmpty()) {
                List<Text> texts = Arrays.stream(text1.getString().split("\n")).map(a -> Text.literal(a)).collect(Collectors.toList());
                drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, texts, mouseX, mouseY);
            }
        }
    }

    public InteractAbleWidget getHoverWidget() {
        return null;
    }

    public interface StatReaderHelper {
        double getValue(ItemStack itemStack);

        boolean hasValue(ItemStack itemStack);
    }
}
