package smartin.miapi.client.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;

public class TransformableWidget extends InteractAbleWidget {

    public Matrix4f rawProjection = new Matrix4f();

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
     * @param title  the Title of the Widget
     */
    public TransformableWidget(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
    }

    public TransformableWidget(int x, int y, int width, int height, float scale) {
        super(x, y, width, height, Text.empty());
        rawProjection = Matrix4f.scale(scale, scale, scale);
    }


    /**
     * This function triggers whenever the mouse is Moved above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     */
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Vector4f position = transFormMousePos(mouseX, mouseY);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.x, position.y)) {
                child.mouseMoved(position.x, position.y);
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    /**
     * This function fires whenever the Mouse is clicked above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @return if this consumes the Click, if you execute an action return true, if not return false
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector4f position = transFormMousePos(mouseX, mouseY);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.x, position.y) && child.mouseClicked(position.x, position.y, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @return if this consumes the Click, if you execute an action return true, if not return false
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector4f position = transFormMousePos(mouseX, mouseY);

        for (Element child : this.children()) {
            if (child.isMouseOver(position.x, position.y) && child.mouseReleased(position.x, position.y, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @param deltaX the Distance dragged X
     * @param deltaY the Distance dragged Y
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Vector4f position = transFormMousePos(mouseX, mouseY);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.x, position.y) && child.mouseDragged(position.x, position.y, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param amount the amount scrolled since the last time this was called
     * @return if this consumes the action, if you execute an action return true, if not return false
     */

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        Vector4f position = transFormMousePos(mouseX, mouseY);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.x, position.y) && child.mouseScrolled(position.x, position.y, amount)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This Function handles if the Mouse is above the current Widget
     * Its not recommended to adjust this if your widget is a Rectangle,
     * if it is not make sure this function returns true whenever the mouse is above the widget
     *
     * @param mouseX current mouseX coordinate
     * @param mouseY current mouseY coordinate
     * @return if the mouseCords are above the Widget
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        Vector4f position = transFormMousePos(mouseX, mouseY);
        for (Element element : children()) {
            if (element.isMouseOver(position.x, position.y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This functions handles the Rendering
     * If you have children you should call super.render(matrices ,mouseX ,mouseY ,delta) at the end to render your children
     *
     * @param matrices the current MatrixStack / PoseStack
     * @param mouseX   current mouseX Position
     * @param mouseY   current mouseY Position
     * @param delta    the deltaTime between frames
     *                 This is needed for animations and co
     */
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

        matrixStack.multiplyPositionMatrix(rawProjection);
        Vector4f position = transFormMousePos(mouseX, mouseY);

        children().forEach(element -> {
            if (element instanceof Drawable drawable) {
                drawable.render(matrixStack, Math.round(position.x), Math.round(position.y), delta);
            }
        });
    }

    @Override
    public void renderHover(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

        matrixStack.multiplyPositionMatrix(rawProjection);
        Vector4f position = transFormMousePos(mouseX, mouseY);

        super.renderHover(matrixStack,  Math.round(position.x), Math.round(position.y), delta);
    }

    public void renderWidget(InteractAbleWidget widget, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

        matrixStack.multiplyPositionMatrix(rawProjection);
        Vector4f position = transFormMousePos(mouseX, mouseY);
        widget.render(matrixStack,  Math.round(position.x), Math.round(position.y), delta);
    }

    public Vector4f transFormMousePos(int mouseX, int mouseY) {
        return transFormMousePos((double)mouseX, mouseY);
    }

    public Vector4f transFormMousePos(double mouseX, double mouseY) {
        Vector4f position = new Vector4f((float) mouseX, (float) mouseY, 0, 0);

        position.transform(getInverse());
        return position;
    }

    public static Vector4f transFormMousePos(double mouseX, double mouseY, Matrix4f matrix4f) {
        Vector4f position = new Vector4f((float) mouseX, (float) mouseY, 0, 0);

        position.transform(matrix4f);
        return position;
    }

    public static boolean inverse(Matrix4f matrix4f) {
        float f = matrix4f.determinantAndAdjugate();
        if (Math.abs(f) > 1.0E-6F) {
            matrix4f.multiply(1 / f);
            return true;
        } else {
            return false;
        }
    }

    public Matrix4f getInverse() {
        Matrix4f inverse = new Matrix4f(rawProjection);
        inverse(inverse);
        return inverse;
    }
}
