package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;

/**
 * This is a widget that displays a scrolling text. The text slowly scrolls through
 * the widget from right to left, with a customizable delay between each character.
 * Once the end of the text is reached, the widget holds for a customizable amount
 * of time before starting again.
 */
@Environment(EnvType.CLIENT)
public class ScrollingTextWidget extends InteractAbleWidget implements Drawable, Element {
    private TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
    private Text text;
    private float timer = 0;
    private int scrollPosition = 0;
    /**
     * The color of the Text, use Minecraft's ColorHelpers to generate
     */
    public int textColor;
    /**
     * The delay in Seconds before the next letter
     */
    public float scrollDelay = 0.5f;
    /**
     * The delay in seconds when reached the end of the message
     */
    public float scrollHoldTime = 2.0f;
    /**
     * Extra time for the first Letter to increase readability
     */
    public float firstLetterExtraTime = 1.0f;
    public boolean hasTextShadow = true;
    private ORIENTATION orientation;

    /**
     * This is a Text that fits within its bounds and slowly scrolls through the Text
     *
     * @param x         the X pos of the Text
     * @param y         the Y pos of the Text
     * @param maxWidth  the max Width of the Text
     * @param text      the text in question
     * @param textColor the TextColor of the Text
     */
    public ScrollingTextWidget(int x, int y, int maxWidth, Text text, int textColor) {
        super(x, y, maxWidth, 9, Text.empty());
        this.textColor = textColor;
        setText(text);
        orientation = ORIENTATION.LEFT;
    }

    /**
     * Calling this resets the position to 0
     *
     * @param text the Text of the scroller
     */
    public void setText(Text text) {
        this.text = text;
        scrollPosition = 0;
        timer = -firstLetterExtraTime;
    }

    public void setOrientation(ORIENTATION orientation) {
        this.orientation = orientation;
    }

    /**
     * either add this as a Child or manually call this method to render the text
     *
     * @param context the current DrawContext
     * @param mouseX  current mouseX Position
     * @param mouseY  current mouseY Position
     * @param delta   the deltaTime between frames
     *                This is needed for animations and co
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String displayText = this.text.getString();

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);

        if (textWidth > this.width) {
            displayText = displayText.substring(scrollPosition);

            textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);

            if (textWidth <= this.width) {
                if (timer > scrollHoldTime) {
                    timer = -firstLetterExtraTime;
                    scrollPosition = 0;

                }
            } else {
                if (timer > scrollDelay) {
                    scrollPosition++;
                    timer = 0;
                }
            }

            timer += delta / 20;

            displayText = MinecraftClient.getInstance().textRenderer.trimToWidth(displayText, this.width);
        }
        int textStart = getX();
        switch (orientation) {
            case CENTERED -> textStart += (this.width - textWidth) / 2;
            case RIGHT -> textStart += (this.width - textWidth);
        }
        context.drawText(renderer, displayText, textStart, this.getY(), textColor, hasTextShadow);
    }

    public int getRequiredWidth() {
        return Math.min(this.width, renderer.getWidth(text));
    }

    public enum ORIENTATION {
        LEFT,
        CENTERED,
        RIGHT
    }
}
