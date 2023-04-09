package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

public class ScrollingTextWidget extends InteractAbleWidget implements Drawable, Element {

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
     * This is a Text that fits within its bounds and slowly scrolls through the Text
     * @param x the X pos of the Text
     * @param y the Y pos of the Text
     * @param maxWidth the max Width of the Text
     * @param text the text in question
     * @param textColor the TextColor of the Text
     */
    public ScrollingTextWidget(int x, int y, int maxWidth, Text text, int textColor) {
        super(x, y, maxWidth, 9, Text.empty());
        this.textColor = textColor;
        setText(text);
    }

    /**
     * Calling this resets the position to 0
     * @param text the Text of the scroller
     */
    public void setText(Text text) {
        this.text = text;
        scrollPosition = 0;
    }

    /**
     * either add this as a Child or manually call this method to render the text
     * @param matrices the current MatrixStack / PoseStack
     * @param mouseX   current mouseX Position
     * @param mouseY   current mouseY Position
     * @param delta    the deltaTime between frames
     *                 This is needed for animations and co
     */
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        String displayText = this.text.getString().trim(); // trim any excess whitespace

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        Miapi.LOGGER.error(" textWidth " + textWidth + " width " + width + " timer " + timer + " pos " + scrollPosition);

        if (textWidth <= this.width) {
            // text fits within available space, no scrolling needed
            this.drawTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, this.text, this.x, this.y, this.textColor);
            return;
        }

        // enable scissor box
        enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);

        displayText = displayText.substring(scrollPosition);

        textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);

        if(textWidth <= this.width){
            if(timer > scrollHoldTime){
                timer = 0;
                scrollPosition = 0;

            }
        }
        else{
            if(timer > scrollDelay){
                scrollPosition++;
                timer = 0;
            }
        }

        timer += delta / 20;

        this.drawTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, Text.of(displayText), this.x, this.y, this.textColor);

        // disable scissor box
        disableScissor();
        RenderSystem.disableScissor();
    }
}
