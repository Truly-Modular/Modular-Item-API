package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;

import java.util.function.Consumer;

public class SimpleButton<T> extends InteractAbleWidget {
    private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/button.png");
    private T toCallback;
    private Consumer<T> callback;
    public boolean isEnabled = true;


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
        ScrollingTextWidget textWidget = new ScrollingTextWidget(x, y, width, title, ColorHelper.Argb.getArgb(255, 200, 200, 200));
        textWidget.hasTextShadow = false;
        this.addChild(textWidget);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderButton(matrices, mouseX, mouseY, delta);
        this.children().forEach(children -> {
            if (children instanceof InteractAbleWidget widget) {
                widget.x = this.x + 2;
                widget.y = Math.max(this.y + 1, this.y + 1 + (this.height - 9) / 2);
                widget.setWidth(this.width - 4);
            }
        });
        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            if(isEnabled){
                callback.accept(toCallback);
                return true;
            }
        }
        return false;
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.setShaderTexture(0, texture);
        int offset = 0;
        if (this.isMouseOver(mouseX, mouseY)) {
            offset = 10;
        }
        if (!isEnabled) {
            offset = 20;
        }
        drawTextureWithEdge(matrices, x, y, offset, 0, 10, 10, this.width, height, 30, 10, 3);
    }
}
