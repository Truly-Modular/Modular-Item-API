package smartin.miapi.client.gui.crafting.crafter.glint;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.config.MiapiConfig;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import static smartin.miapi.client.gui.InteractAbleWidget.drawSquareBorder;

public class FloatWidgetField extends EditBox implements GuiEventListener {
    public @Nullable String prefix;
    public float value;
    public @Nullable Float maxValue = null;
    public @Nullable Float minValue = null;
    protected final Consumer<Float> onValueUpdate;
    protected final Font textRenderer;
    public DecimalFormat format = new DecimalFormat("###.##");


    public FloatWidgetField(Font textRenderer, int x, int y, int width, int height, Component message, Consumer<Float> onValueUpdate) {
        super(textRenderer, x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.onValueUpdate = onValueUpdate;
        setValue(value);
        setBordered(false);
    }

    public FloatWidgetField(Font textRenderer, int x, int y, int width, int height, float minValue, float maxValue, Component message, Consumer<Float> onValueUpdate, String prefix) {
        this(textRenderer, x, y, width, height, message, onValueUpdate);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.prefix = prefix;
    }

    public void setValue(float v) {
        if (maxValue != null && v > maxValue) v = maxValue;
        else if (minValue != null && v < minValue) v = minValue;
        value = v;
        setValue(prefix + format.format(Float.valueOf(v)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.setFocused(isMouseOver(mouseX, mouseY));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void tryUpdateValue() {
        try {
            value = Float.parseFloat(getValue());
        } catch (Exception e) {
            value = 0;
        }

        if (maxValue != null && value > maxValue) {
            value = maxValue;
            setValue(value);
        } else if (minValue != null && value < minValue) {
            value = minValue;
            setValue(value);
        }

        onValueUpdate.accept(value);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (prefix != null)
            context.drawString(textRenderer, prefix, getX() - textRenderer.width(prefix) - 4, getY() - 1, Color.WHITE.argb(), true);

        if ((MiapiConfig.INSTANCE.server.other.developmentMode) && Screen.hasAltDown())
            drawSquareBorder(context, getX(), getY(), getWidth(), getHeight(), 1, Color.YELLOW.argb());
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public void insertText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (!((chr >= '0' && chr <= '9') || (chr == '.' && !getValue().contains(".")))) return;
        }
        super.insertText(text);
        tryUpdateValue();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result) tryUpdateValue();
        return result;
    }
}