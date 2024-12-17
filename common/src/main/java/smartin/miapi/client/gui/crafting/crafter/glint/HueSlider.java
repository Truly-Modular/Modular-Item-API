package smartin.miapi.client.gui.crafting.crafter.glint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.Nucleus;
import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class HueSlider extends AbstractWidget {
    public static final ResourceLocation SCROLLBAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nucleus.MOD_ID, "textures/gui/small_scrollbars.png");

    public float value;
    public boolean dragging = false;
    public final Consumer<Float> updateListener;

    public HueSlider(int x, int y, int width, int height, Consumer<Float> updateListener) {
        super(x, y, width, height, Component.empty());
        this.updateListener = updateListener;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragging = true;
        value = MathUtil.clamp((float) (mouseX - getX()) / width, 0f, 1f);
        update();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragging = false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        value = MathUtil.clamp((float) (mouseX-getX())/width, 0f, 1f);
        update();
    }

    protected boolean isDragging(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) || dragging;
    }

    protected boolean isReleasing(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) || dragging;
    }

    protected void update() {
        updateListener.accept(value);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderScrollbar(context, mouseX, mouseY, delta);
        float startY = getY();
        float endY = getY()+height-2;
        int len = Color.RAINBOW.length;
        float segmentSize = (float) width/len;
        VertexConsumer vc = context.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = context.pose().last().pose();
        for (int i = 0; i < len; i++) {
            Color current = Color.RAINBOW[i];
            Color next = Color.RAINBOW[(i+1) % len];

            float startX = getX()+i*segmentSize;
            float endX = getX()+(i+1)*segmentSize;

            vc.addVertex(matrix4f, startX, startY, 0).setColor(current.r(), current.g(), current.b(), current.a());
            vc.addVertex(matrix4f, startX, endY, 0).setColor(current.r(), current.g(), current.b(), current.a());
            vc.addVertex(matrix4f, endX, endY, 0).setColor(next.r(), next.g(), next.b(), next.a());
            vc.addVertex(matrix4f, endX, startY, 0).setColor(next.r(), next.g(), next.b(), next.a());
        }
    }

    protected void renderScrollbar(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().pushPose();
        context.pose().translate(0, 0, 1);
        context.blit(SCROLLBAR_TEXTURE, getX() - 1 + (int) (value*(width-getScrollbarWidth())), getY()-1, 6, 8, isHovered() ? 6 : 0, 0, 6, 8, 12, 8);
        context.pose().popPose();
    }

    protected int getScrollbarWidth() {
        return 4;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
