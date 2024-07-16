package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import smartin.miapi.client.gui.crafting.CraftingScreen;

import java.util.function.Consumer;

/**
 * A simple button widget with a callback that accepts a generic argument.
 *
 * @param <T> The type of the argument accepted by the callback.
 */
@Environment(EnvType.CLIENT)
public class SimpleButton<T> extends InteractAbleWidget {
    private final T toCallback;
    private final Consumer<T> callback;
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
    public SimpleButton(int x, int y, int width, int height, Component title, T toCallback, Consumer<T> callback) {
        super(x, y, width, height, title);
        assert callback != null;
        this.toCallback = toCallback;
        this.callback = callback;
        ScrollingTextWidget textWidget = new ScrollingTextWidget(x, y, width, title, FastColor.ARGB32.color(255, 255, 255, 255));
        textWidget.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
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
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
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
            playClickedSound();
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
    public void renderButton(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        int offset = 0;
        if (this.isMouseOver(mouseX, mouseY)) {
            offset = 10;
        }
        if (!isEnabled) {
            offset = 20;
        }
        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 339 + offset, 165, 10, 10, getWidth(), getHeight(), 512, 512, 3);
    }
}
