package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector4f;
import org.joml.Vector4i;

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
    private Orientation orientation;

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
        orientation = Orientation.LEFT;
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

    /**
     * get the current Text
     *
     * @return the Text of the Widget
     */
    public Text getText() {
        return text;
    }

    public void setOrientation(Orientation orientation) {
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
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
        boolean scissorEnabled = false;
        int textStart = getX();
        switch (orientation) {
            case CENTERED -> textStart += (width - textWidth) / 2;
            case RIGHT -> textStart += (width - textWidth);
        }

        if (textWidth > width) {
            String string = text.getString();
            int offsetAmount = 0;
            if (scrollPosition < string.length()) {
                String sub = string.substring(0, scrollPosition);
                int subLength = MinecraftClient.getInstance().textRenderer.getWidth(sub);
                if (subLength < width)
                    offsetAmount = subLength;
            }

            /*textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);

            if (textWidth <= width) {
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

            displayText = MinecraftClient.getInstance().textRenderer.trimToWidth(displayText, width);*/
            timer += delta / 20;
            if (timer > scrollDelay) {
                scrollPosition++;
                timer = 0;
            }

            textStart-=offsetAmount;
            Vector4f corner1 = TransformableWidget.transFormMousePos(getX(), getY(), context.getMatrices().peek().getPositionMatrix());
            Vector4f corner2 = TransformableWidget.transFormMousePos(getX()+width, getY()+height, context.getMatrices().peek().getPositionMatrix());
            context.enableScissor((int) corner1.x, (int) corner1.y, (int) corner2.x+1, (int) corner2.y);
            scissorEnabled = true;
        }
        context.drawText(renderer, text, textStart, getY(), textColor, hasTextShadow);
        if (scissorEnabled) context.disableScissor();
    }

    public int getRequiredWidth() {
        return Math.min(this.width, renderer.getWidth(text));
    }

    public enum Orientation {
        LEFT,
        CENTERED,
        RIGHT
    }
}
