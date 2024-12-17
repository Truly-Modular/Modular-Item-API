package smartin.miapi.client.gui.crafting.crafter.glint;

import com.redpxnda.nucleus.config.screen.widget.IntegerFieldWidget;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;

import java.util.function.Consumer;

public class ColorPickerWidget extends InteractAbleWidget {
    public static final ResourceLocation TEXTURE = CraftingScreen.BACKGROUND_TEXTURE;//ResourceLocation.fromNamespaceAndPath(Nucleus.MOD_ID, "textures/gui/color_picker.png");


    protected final Font textRenderer;
    protected final Consumer<Color> onUpdate;
    protected final ColorGrid colorGrid;
    protected final HueSlider hueSlider;
    protected final AlphaSlider alphaSlider;
    protected Color hueColor = Color.RED.copy();
    protected Color color = Color.RED;

    protected final IntegerFieldWidget redField;
    protected final IntegerFieldWidget greenField;
    protected final IntegerFieldWidget blueField;
    protected final IntegerFieldWidget hueField;
    protected final IntegerFieldWidget satField;
    protected final IntegerFieldWidget lightField;
    protected final IntegerFieldWidget alphaField;

    public ColorPickerWidget(Font textR, int y, int x, Consumer<Color> updateListener) {
        super(x, y, 128, 92, Component.empty());
        textRenderer = textR;
        onUpdate = updateListener;
        colorGrid = new ColorGrid(x + 8, y + 8, 76, 76, c -> updateColor());
        hueSlider = new HueSlider(x + 20, y + 94, 64, 8, f -> updateHue());
        alphaSlider = new AlphaSlider(x + 20, y + 110, 64, 8, f -> updateAlpha());

        redField = new IntegerFieldWidget(textR, x + 101, y + 9, 30, 9, 0, 255, Component.empty(), i -> setColorAndUpdate(color.withRed(i)), "ʀ");
        greenField = new IntegerFieldWidget(textR, x + 101, y + 22, 30, 9, 0, 255, Component.empty(), i -> setColorAndUpdate(color.withGreen(i)), "ɢ");
        blueField = new IntegerFieldWidget(textR, x + 101, y + 35, 30, 9, 0, 255, Component.empty(), i -> setColorAndUpdate(color.withBlue(i)), "ʙ");
        hueField = new IntegerFieldWidget(textR, x + 101, y + 50, 30, 9, 0, 360, Component.empty(), i -> setColorAndUpdate(i / 360f, colorGrid.saturation, colorGrid.lightness), "ʜ");
        satField = new IntegerFieldWidget(textR, x + 101, y + 63, 30, 9, 0, 100, Component.empty(), i -> setColorAndUpdate(hueSlider.value, i / 100f, colorGrid.lightness), "ѕ");
        lightField = new IntegerFieldWidget(textR, x + 101, y + 76, 30, 9, 0, 100, Component.empty(), i -> setColorAndUpdate(hueSlider.value, colorGrid.saturation, i / 100f), "ʟ");
        alphaField = new IntegerFieldWidget(textR, x + 101, y + 108, 30, 9, 0, 100, Component.empty(), i -> setColorAndUpdate(color.withAlpha(i / 100f)), "ᴀ");
    }

    public ColorPickerWidget(Font textRenderer, int x, int y, Consumer<Color> onUpdate, ColorGrid ColorGrid, HueSlider hueSlider, AlphaSlider alphaSlider, IntegerFieldWidget redField, IntegerFieldWidget greenField, IntegerFieldWidget blueField, IntegerFieldWidget hueField, IntegerFieldWidget satField, IntegerFieldWidget lightField, IntegerFieldWidget alphaField) {
        super(x, y, 128, 128, Component.empty());
        this.textRenderer = textRenderer;
        this.onUpdate = onUpdate;
        this.colorGrid = ColorGrid;
        this.hueSlider = hueSlider;
        this.alphaSlider = alphaSlider;
        this.redField = redField;
        this.greenField = greenField;
        this.blueField = blueField;
        this.hueField = hueField;
        this.satField = satField;
        this.lightField = lightField;
        this.alphaField = alphaField;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        colorGrid.setX(x + 8);
        hueSlider.setX(x + 20);
        alphaSlider.setX(x + 20);

        redField.setX(x + 101);
        greenField.setX(x + 101);
        blueField.setX(x + 101);
        hueField.setX(x + 101);
        satField.setX(x + 101);
        lightField.setX(x + 101);
        alphaField.setX(x + 101);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        colorGrid.setY(y + 8);
        hueSlider.setY(y + 94);
        alphaSlider.setY(y + 110);

        redField.setY(y + 9);
        greenField.setY(y + 22);
        blueField.setY(y + 35);
        hueField.setY(y + 50);
        satField.setY(y + 63);
        lightField.setY(y + 76);
        alphaField.setY(y + 108);
    }

    public void setTextFieldValues() {
        redField.setValue(color.r());
        greenField.setValue(color.g());
        blueField.setValue(color.b());

        hueField.setValue(Math.round(hueSlider.value * 360));
        satField.setValue(Math.round(colorGrid.saturation * 100));
        lightField.setValue(Math.round(colorGrid.lightness * 100));

        alphaField.setValue(Math.round(alphaSlider.value * 100));
    }

    public void setColor(float hue, float saturation, float lightness) {
        hueSlider.value = MathUtil.clamp(hue, 0, 1);
        colorGrid.saturation = MathUtil.clamp(saturation, 0, 1);
        colorGrid.lightness = MathUtil.clamp(lightness, 0, 1);
        updateHueNoUpdate();
        color = colorGrid.calculateColor();
        color.setAlpha(alphaSlider.value);
        setTextFieldValues();
    }

    public void setColorAndUpdate(float hue, float saturation, float lightness) {
        setColor(hue, saturation, lightness);
        onUpdate.accept(color);
    }

    public void setColor(Color clr) {
        float[] hsl = MathUtil.rgbToHsv(clr.r(), clr.g(), clr.b());
        color = clr;
        hueSlider.value = MathUtil.clamp(hsl[0] / 360, 0, 1);
        alphaSlider.value = clr.alphaAsFloat();
        colorGrid.saturation = MathUtil.clamp(hsl[1], 0, 1);
        colorGrid.lightness = MathUtil.clamp(hsl[2], 0, 1);
        updateHueNoUpdate();
        colorGrid.calculateColor();
        setTextFieldValues();
    }

    public void setColorAndUpdate(Color clr) {
        setColor(clr);
        onUpdate.accept(color);
    }

    public void updateColor() {
        color = colorGrid.color.copy();
        color.setAlpha(alphaSlider.value);
        setTextFieldValues();
        onUpdate.accept(color);
    }

    public void updateAlpha() {
        color.setAlpha(alphaSlider.value);
        updateColor();
    }

    public void updateHueNoUpdate() {
        int len = Color.RAINBOW.length;
        float pos = hueSlider.value * len;
        int index = (int) pos;
        float delta = pos - index;
        Color left = Color.RAINBOW[index % len];
        Color right = Color.RAINBOW[(index + 1) % len];
        left.lerp(delta, right, hueColor);
        colorGrid.hueColor = hueColor;
    }

    public void updateHue() {
        updateHueNoUpdate();
        colorGrid.update();
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(TEXTURE, getX(), getY(), 0, 0, 295, 128, 92, 512, 512);

        colorGrid.render(context, mouseX, mouseY, delta);
        //hueSlider.render(context, mouseX, mouseY, delta);
        //alphaSlider.render(context, mouseX, mouseY, delta);

        redField.render(context, mouseX, mouseY, delta);
        greenField.render(context, mouseX, mouseY, delta);
        blueField.render(context, mouseX, mouseY, delta);
        hueField.render(context, mouseX, mouseY, delta);
        satField.render(context, mouseX, mouseY, delta);
        lightField.render(context, mouseX, mouseY, delta);
        //alphaField.render(context, mouseX, mouseY, delta);

        //context.fill(getX() + 8, getY() + 94, getX() + 14, getY() + 100, hueColor.argb());
        //context.fill(getX() + 8, getY() + 110, getX() + 14, getY() + 116, FastColor.ARGB32.color((int) (alphaSlider.value * 255), 255, 255, 255));
        //context.fill(getX() + 90, getY() + 93, getX() + 120, getY() + 101, color.argb());
    }

    public void unfocusTextFields() {
        redField.setFocused(false);
        blueField.setFocused(false);
        greenField.setFocused(false);
        hueField.setFocused(false);
        satField.setFocused(false);
        lightField.setFocused(false);
        alphaField.setFocused(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        unfocusTextFields();
        if (colorGrid.isMouseOver(mouseX, mouseY) && colorGrid.mouseClicked(mouseX, mouseY, button)) return true;
        if (hueSlider.isMouseOver(mouseX, mouseY) && hueSlider.mouseClicked(mouseX, mouseY, button)) return true;
        if (alphaSlider.isMouseOver(mouseX, mouseY) && alphaSlider.mouseClicked(mouseX, mouseY, button)) return true;

        if (redField.isMouseOver(mouseX, mouseY)) {
            redField.setFocused(true);
            return redField.mouseClicked(mouseX, mouseY, button);
        }
        if (greenField.isMouseOver(mouseX, mouseY)) {
            greenField.setFocused(true);
            return greenField.mouseClicked(mouseX, mouseY, button);
        }
        if (blueField.isMouseOver(mouseX, mouseY)) {
            blueField.setFocused(true);
            return blueField.mouseClicked(mouseX, mouseY, button);
        }

        if (hueField.isMouseOver(mouseX, mouseY)) {
            hueField.setFocused(true);
            return hueField.mouseClicked(mouseX, mouseY, button);
        }
        if (satField.isMouseOver(mouseX, mouseY)) {
            satField.setFocused(true);
            return satField.mouseClicked(mouseX, mouseY, button);
        }
        if (lightField.isMouseOver(mouseX, mouseY)) {
            lightField.setFocused(true);
            return lightField.mouseClicked(mouseX, mouseY, button);
        }

        if (alphaField.isMouseOver(mouseX, mouseY)) {
            alphaField.setFocused(true);
            return alphaField.mouseClicked(mouseX, mouseY, button);
        }
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean bl = this.clicked(mouseX, mouseY);
                if (bl) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (colorGrid.isDragging(mouseX, mouseY) && colorGrid.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        if (hueSlider.isDragging(mouseX, mouseY) && hueSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        if (alphaSlider.isDragging(mouseX, mouseY) && alphaSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (colorGrid.isReleasing(mouseX, mouseY) && colorGrid.mouseReleased(mouseX, mouseY, button)) return true;
        if (hueSlider.isReleasing(mouseX, mouseY) && hueSlider.mouseReleased(mouseX, mouseY, button)) return true;
        if (alphaSlider.isReleasing(mouseX, mouseY) && alphaSlider.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (redField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (greenField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (blueField.keyPressed(keyCode, scanCode, modifiers)) return true;

        if (hueField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (satField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (lightField.keyPressed(keyCode, scanCode, modifiers)) return true;

        if (alphaField.keyPressed(keyCode, scanCode, modifiers)) return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (redField.charTyped(chr, modifiers)) return true;
        if (greenField.charTyped(chr, modifiers)) return true;
        if (blueField.charTyped(chr, modifiers)) return true;

        if (hueField.charTyped(chr, modifiers)) return true;
        if (satField.charTyped(chr, modifiers)) return true;
        if (lightField.charTyped(chr, modifiers)) return true;

        if (alphaField.charTyped(chr, modifiers)) return true;

        return super.charTyped(chr, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
