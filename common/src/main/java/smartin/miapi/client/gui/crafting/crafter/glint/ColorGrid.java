package smartin.miapi.client.gui.crafting.crafter.glint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class ColorGrid extends AbstractWidget {
    public float saturation = 1;
    public float lightness = 1;
    public Color hueColor = Color.RED.copy();
    public Color color = Color.RED.copy();
    public boolean dragging = false;
    public final Consumer<Color> updateListener;

    public ColorGrid(int x, int y, int width, int height, Consumer<Color> updateListener) {
        super(x, y, width, height, Component.empty());
        this.updateListener = updateListener;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragging = true;
        saturation = Mth.clamp((float) (mouseX - getX()) / width, 0f, 1f);
        lightness = Mth.clamp(1f - (float)(mouseY-getY())/height, 0f, 1f);
        update();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragging = false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        saturation = Mth.clamp((float) (mouseX-getX())/width, 0f, 1f);
        lightness = Mth.clamp(1f - (float)(mouseY-getY())/height, 0f, 1f);
        update();
    }

    protected boolean isDragging(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) || dragging;
    }

    protected boolean isReleasing(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) || dragging;
    }

    protected Color calculateColor() {
        color = Color.WHITE.copy();
        color.lerp(saturation, hueColor);
        color.lerp(1-lightness, Color.BLACK);
        return color;
    }

    protected void update() {
        calculateColor();
        updateListener.accept(color);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        VertexConsumer vc = context.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = context.pose().last().pose();

        float iLightness = 1-lightness;
        vc.addVertex(matrix4f, getX() + saturation*width - 3, getY() + iLightness*height - 3, 1).setColor(Color.WHITE.r(), Color.WHITE.g(), Color.WHITE.b(), Color.WHITE.a());
        vc.addVertex(matrix4f, getX() + saturation*width - 3, getY() + iLightness*height + 3, 1).setColor(Color.WHITE.r(), Color.WHITE.g(), Color.WHITE.b(), Color.WHITE.a());
        vc.addVertex(matrix4f, getX() + saturation*width + 3, getY() + iLightness*height + 3, 1).setColor(Color.WHITE.r(), Color.WHITE.g(), Color.WHITE.b(), Color.WHITE.a());
        vc.addVertex(matrix4f, getX() + saturation*width + 3, getY() + iLightness*height - 3, 1).setColor(Color.WHITE.r(), Color.WHITE.g(), Color.WHITE.b(), Color.WHITE.a());

        vc.addVertex(matrix4f, getX() + saturation*width - 2, getY() + iLightness*height - 2, 1).setColor(color.r(), color.g(), color.b(), color.a());
        vc.addVertex(matrix4f, getX() + saturation*width - 2, getY() + iLightness*height + 2, 1).setColor(color.r(), color.g(), color.b(), color.a());
        vc.addVertex(matrix4f, getX() + saturation*width + 2, getY() + iLightness*height + 2, 1).setColor(color.r(), color.g(), color.b(), color.a());
        vc.addVertex(matrix4f, getX() + saturation*width + 2, getY() + iLightness*height - 2, 1).setColor(color.r(), color.g(), color.b(), color.a());

        for (int i = 0; i < height; i++) {
            float delt = (float) i/height;

            Color left = Color.WHITE.copy();
            left.lerp(delt, Color.BLACK);

            Color right = hueColor.copy();
            right.lerp(delt, Color.BLACK);

            vc.addVertex(matrix4f, getX(), getY()+i, 0).setColor(left.r(), left.g(), left.b(), left.a());
            vc.addVertex(matrix4f, getX(), getY()+i+1, 0).setColor(left.r(), left.g(), left.b(), left.a());
            vc.addVertex(matrix4f, getX()+width, getY()+i+1, 0).setColor(right.r(), right.g(), right.b(), right.a());
            vc.addVertex(matrix4f, getX()+width, getY()+i, 0).setColor(right.r(), right.g(), right.b(), right.a());
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
