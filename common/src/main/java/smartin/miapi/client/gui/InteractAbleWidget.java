package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class InteractAbleWidget extends ClickableWidget implements Drawable, Element {
    protected final List<Element> children = new ArrayList<>();

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
    public InteractAbleWidget(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
    }

    /*
    Helper Functions for General use
     */

    /**
     * This functions Renders a Square Border
     *
     * @param matrices
     * @param x           top left Corner x
     * @param y           top Left Corner <
     * @param width       width of the square
     * @param height      height of the square
     * @param borderWidth with of the border, border is drawn inwards
     * @param color       color of the border
     */
    public static void drawSquareBorder(MatrixStack matrices, int x, int y, int width, int height, int borderWidth, int color) {
        //Top
        fill(matrices, x, y, x + width, y + borderWidth, color);
        //Bottom
        fill(matrices, x, y + height, x + width, y + height - borderWidth, color);
        //Left
        fill(matrices, x, y, x + borderWidth, y + height, color);
        //Right
        fill(matrices, x + width, y, x + width - borderWidth, y + height, color);
    }


    /**
     * Draws a texture with an edge around its border. The texture can be limited to a portion of itself by specifying the
     * coordinates (u, v) and (u2, regionHeight).
     * This is useful if a texture might be resized in the UI to still align its Edges
     *
     * @param matrices      The matrix stack to apply the transformation to.
     * @param x             The x-coordinate to draw the texture at.
     * @param y             The y-coordinate to draw the texture at.
     * @param u             The x-coordinate of the top-left corner of the texture region to draw.
     * @param v             The y-coordinate of the top-left corner of the texture region to draw.
     * @param regionWidth   The x-coordinate of the bottom-right corner of the texture region to draw.
     * @param regionHeight  The y-coordinate of the bottom-right corner of the texture region to draw.
     * @param width         The width of the texture.
     * @param height        The height of the texture.
     * @param textureWidth  The width of the texture sheet.
     * @param textureHeight The height of the texture sheet.
     * @param borderWidth   The width of the border to draw around the texture.
     */
    public static void drawTextureWithEdge(MatrixStack matrices, int x, int y, int u, int v, int regionWidth, int regionHeight, int width, int height, int textureWidth, int textureHeight, int borderWidth) {
        //Top Left Corner
        drawTexture(matrices, x, y, borderWidth, borderWidth, u, v, borderWidth, borderWidth, textureWidth, textureHeight);
        //Top Right Corner
        drawTexture(matrices, x + width - borderWidth, y, borderWidth, borderWidth, u + regionWidth - borderWidth, v, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Left Corner
        drawTexture(matrices, x, y + height - borderWidth, borderWidth, borderWidth, u, v + regionHeight - borderWidth, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Right Corner
        drawTexture(matrices, x + width - borderWidth, y + height - borderWidth, borderWidth, borderWidth, u + regionWidth - borderWidth, v + regionHeight - borderWidth, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Bar
        drawTexture(matrices, x + borderWidth, y + height - borderWidth, width - 2 * borderWidth, borderWidth, u + borderWidth, v + regionHeight - borderWidth, regionWidth - borderWidth * 2, borderWidth, textureWidth, textureHeight);
        //Right Bar
        drawTexture(matrices, x + width - borderWidth, y + borderWidth, borderWidth, height - 2 * borderWidth, u + regionWidth - borderWidth, v + borderWidth, borderWidth, regionHeight - borderWidth * 2, textureWidth, textureHeight);
        //Left Bar
        drawTexture(matrices, x, y + borderWidth, borderWidth, height - 2 * borderWidth, u, v + borderWidth, u + borderWidth, regionHeight - borderWidth * 2, textureWidth, textureHeight);
        //Top Bar
        drawTexture(matrices, x + borderWidth, y, width - 2 * borderWidth, borderWidth, u + borderWidth, v, regionWidth - borderWidth * 2, borderWidth, textureWidth, textureHeight);
        //Center
        drawTexture(matrices, x + borderWidth, y + borderWidth, width - 2 * borderWidth, height - 2 * borderWidth, u + borderWidth, v + borderWidth, regionWidth - borderWidth * 2, regionHeight - borderWidth * 2, textureWidth, textureHeight);
    }


    /**
     * Draws a texture with an edge around its border. The texture can be limited to a portion of itself by specifying the
     * coordinates (u, v) and (u2, v2).
     * This is useful if a texture might be resized in the UI to still align its Edges
     *
     * @param matrices      The matrix stack to apply the transformation to.
     * @param x             The x-coordinate to draw the texture at.
     * @param y             The y-coordinate to draw the texture at.
     * @param width         The width of the texture.
     * @param height        The height of the texture.
     * @param textureWidth  The width of the texture sheet.
     * @param textureHeight The height of the texture sheet.
     * @param borderWidth   The width of the border to draw around the texture.
     */
    public static void drawTextureWithEdge(MatrixStack matrices, int x, int y, int width, int height, int textureWidth, int textureHeight, int borderWidth) {
        drawTextureWithEdge(matrices, x, y, 0, 0, textureWidth, textureHeight, width, height, textureWidth, textureHeight, borderWidth);
    }


    /**
     * This Method adds a Child to this Widget
     * native Access to the Array is given in children()
     *
     * @param element the Child to be Added
     */
    public void addChild(Element element) {
        children().add(element);
    }

    /**
     * This Method removes a child Element from this Widget
     * native Access to the Array is given in children()
     *
     * @param element
     */
    public void removeChild(Element element) {
        children().remove(element);
    }

    /**
     * This Method gives direct access to the ArrayList of Children
     */
    @Nonnull
    public List<Element> children() {
        return children;
    }

    /**
     * This function triggers whenever the mouse is Moved above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     */
    public void mouseMoved(double mouseX, double mouseY) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseMoved(mouseX, mouseY);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    Miapi.LOGGER.error("Consumpted Click" + child.toString());
                    return true;
                }
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    return true;
                }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseScrolled(mouseX, mouseY, amount)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param keyCode   the keyCode of the pressed Key
     * @param scanCode  the scanCode of the pressed Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element child : this.children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param keyCode   the keyCode of the released Key
     * @param scanCode  the scanCode of the released Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Element child : this.children()) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param chr       the Character typed
     * @param modifiers if keys like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean charTyped(char chr, int modifiers) {
        for (Element child : this.children()) {
            if (child.charTyped(chr, modifiers)) {
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
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    public void setHeight(int height){
        this.height = height;
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();
        children().forEach(element -> {
            if (element instanceof Drawable drawable) {
                drawable.render(matrices, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }
}
