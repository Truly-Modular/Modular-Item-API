package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;

import java.util.function.Consumer;

/**
 * A simple button widget with a callback that accepts a generic argument.
 *
 * @param <T> The type of the argument accepted by the callback.
 */
@Environment(EnvType.CLIENT)
public class SimpleButton<T> extends InteractAbleWidget {
    private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/button.png");
    private T toCallback;
    private Consumer<T> callback;
    public boolean isEnabled = true;

    /**
     * Creates a new instance of {@link SimpleButton}.
     *
     * @param x          The x coordinate of the button.
     * @param y          The y coordinate of the button.
     * @param width      The width of the button.
     * @param height     The height of the button.
     * @param title      The title of the button.
     * @param toCallback The argument to pass to the callback.
     * @param callback   The callback to invoke when the button is clicked.
     */
    public SimpleButton(int x, int y, int width, int height, Text title, T toCallback, Consumer<T> callback) {
        super(x, y, width, height, title);
        this.toCallback = toCallback;
        this.callback = callback;
        ScrollingTextWidget textWidget = new ScrollingTextWidget(x, y, width, title, ColorHelper.Argb.getArgb(255, 200, 200, 200));
        textWidget.setOrientation(ScrollingTextWidget.ORIENTATION.CENTERED);
        textWidget.hasTextShadow = false;
        this.addChild(textWidget);
    }

    /**
     * Renders the button on the screen.
     *
     * @param drawContext The matrix stack.
     * @param mouseX      The x position of the mouse.
     * @param mouseY      The y position of the mouse.
     * @param delta       The time since the last tick.
     */
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderButton(drawContext, mouseX, mouseY, delta);
        this.children().forEach(children -> {
            if (children instanceof InteractAbleWidget widget) {
                widget.setX(this.getX() + 2);
                widget.setY(Math.max(this.getY() + 1, this.getY() + 1 + (this.height - 9) / 2));
                widget.setWidth(this.width - 4);
            }
        });
        super.render(drawContext, mouseX, mouseY, delta);
    }

    /**
     * Handles a mouse click event on the button.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param button The button that was clicked.
     * @return {@code true} if the button was clicked, {@code false} otherwise.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && (isEnabled)) {
                callback.accept(toCallback);
                return true;

        }
        return false;
    }

    /**
     * Renders the button texture.
     *
     * @param drawContext The matrix stack.
     * @param mouseX      The x position of the mouse.
     * @param mouseY      The y position of the mouse.
     * @param delta       The time since the last tick.
     */
    public void renderButton(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int offset = 0;
        if (this.isMouseOver(mouseX, mouseY)) {
            offset = 10;
        }
        if (!isEnabled) {
            offset = 20;
        }
        drawTextureWithEdge(drawContext,texture, getX(), getY(), offset, 0, 10, 10, this.width, height, 30, 10, 3);
    }
}
