package smartin.miapi.client.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
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


    /**
     * This function triggers whenever the mouse is Moved above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     */
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.getX(), position.getY())) {
                child.mouseMoved(position.getX(), position.getY());
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
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.getX(), position.getY()) && child.mouseClicked(position.getX(), position.getY(), button)) {
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
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);

        for (Element child : this.children()) {
            if (child.isMouseOver(position.getX(), position.getY()) && child.mouseReleased(position.getX(), position.getY(), button)) {
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
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.getX(), position.getY()) && child.mouseDragged(position.getX(), position.getY(), button, deltaX, deltaY)) {
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
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);
        for (Element child : this.children()) {
            if (child.isMouseOver(position.getX(), position.getY()) && child.mouseScrolled(position.getX(), position.getY(), amount)) {
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
        Vec3f position = new Vec3f((float) mouseX, (float) mouseY, 0);
        Matrix3f matrix3f = new Matrix3f(rawProjection);
        matrix3f.multiply(-1);
        position.transform(matrix3f);
        return super.isMouseOver(position.getX(), position.getY());
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
        //rawProjection.
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.loadIdentity();
        matrix4f.multiply(rawProjection);
        Matrix4f matrix4f2 = new Matrix4f();
        matrix4f2.loadIdentity();
        matrix4f2.multiply(matrix4f);
        //Miapi.LOGGER.error(String.valueOf(matrix4f2.invert()));

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

        matrixStack.multiplyPositionMatrix(matrix4f);
        Vector4f position = new Vector4f(mouseX, mouseY, 0, 0);

        position.transform(matrix4f2);
        //Miapi.LOGGER.error(mouseX + "  " + position.getX());
        children().forEach(element -> {
            if (element instanceof Drawable drawable) {
                drawable.render(matrixStack, (int) position.getX(), (int) position.getY(), delta);
            }
        });
        //matrixStack.pop();
    }
}
