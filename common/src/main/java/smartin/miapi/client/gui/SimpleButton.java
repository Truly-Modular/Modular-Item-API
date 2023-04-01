package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;

import java.util.function.Consumer;

public class SimpleButton<T> extends InteractAbleWidget {
    private T toCallback;
    private Consumer<T> callback;

    /**
     * This is a Widget build to support Children and parse the events down to them.
     * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
     * unlike the base vanilla classes.
     * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
     * super method or handle the children yourself
     *
     * @param x      the X Position
     * @param y      the y Position
     * @param width  the width
     * @param height the height
     *               These for Params above are used to create feedback on isMouseOver() by default
     * @param title
     */
    public SimpleButton(int x, int y, int width, int height, Text title, T toCallBack, Consumer<T> callback) {
        super(x, y, width, height, title);
        this.toCallback = toCallBack;
        this.callback = callback;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(false);
        this.renderButton(matrices, mouseX, mouseY, delta);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Miapi.LOGGER.warn("clickedBottomButton");
        callback.accept(toCallback);
        return true;
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int color = ColorHelper.Argb.getArgb(255, 50, 50, 50);

        if (this.isMouseOver(mouseX, mouseY)) {
            color = ColorHelper.Argb.getArgb(255, 100, 100, 100);
        }
        drawSquareBorder(matrices, this.x, this.y, this.width, this.height, 1, color);
        MinecraftClient.getInstance().textRenderer.draw(matrices, this.getMessage(), this.x + 2, this.y + 2, ColorHelper.Argb.getArgb(255, 59, 59, 59));
    }
}
