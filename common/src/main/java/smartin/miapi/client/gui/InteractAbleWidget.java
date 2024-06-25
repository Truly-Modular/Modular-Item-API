package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2d;
import smartin.miapi.config.MiapiConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An interactive widget that supports children and forwards events to them.
 * Use this class in conjunction with the ParentHandledScreen to correctly handle children.
 * If you want to handle some events yourself and still support children, you should call the
 * corresponding super method or handle the children yourself.
 */
@Environment(EnvType.CLIENT)
public abstract class InteractAbleWidget extends ClickableWidget implements Drawable, Element {
    protected final List<Element> children = new ArrayList<>();
    protected final List<InteractAbleWidget> hoverElements = new ArrayList<>();
    public boolean debug = false;
    public int randomColor = ColorHelper.Argb.getArgb(180, (int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255));

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
    protected InteractAbleWidget(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
    }

    /*
    Helper Functions for General use
     */

    /**
     * This functions Renders a Square Border
     *
     * @param drawContext The drawContext used.
     * @param x           top left Corner x
     * @param y           top Left Corner <
     * @param width       width of the square
     * @param height      height of the square
     * @param borderWidth with of the border, border is drawn inwards
     * @param color       color of the border
     */
    public static void drawSquareBorder(DrawContext drawContext, int x, int y, int width, int height, int borderWidth, int color) {
        //Top
        drawContext.fill(x, y, x + width, y + borderWidth, 100, color);
        //Bottom
        drawContext.fill(x, y + height, x + width, y + height - borderWidth, 100, color);
        //Left
        drawContext.fill(x, y, x + borderWidth, y + height, 100, color);
        //Right
        drawContext.fill(x + width, y, x + width - borderWidth, y + height, 100, color);
    }

    /**
     * Draws a texture with an edge around its border. The texture can be limited to a portion of itself by specifying the
     * coordinates (u, v) and (u2, regionHeight).
     * This is useful if a texture might be resized in the UI to still align its Edges
     *
     * @param drawContext   The drawContext used.
     * @param texture       The texture rendered.
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
    public static void drawTextureWithEdgeAndScale(DrawContext drawContext, Identifier texture, int x, int y, int u, int v, int regionWidth, int regionHeight, int width, int height, int textureWidth, int textureHeight, int borderWidth, float scale) {
        DrawContext context = new DrawContext(MinecraftClient.getInstance(), drawContext.getVertexConsumers());
        context.getMatrices().multiplyPositionMatrix(drawContext.getMatrices().peek().getPositionMatrix());
        context.getMatrices().peek().getPositionMatrix().scale(1 / scale);
        drawTextureWithEdge(context, texture, (int) (x * scale), (int) (y * scale), u, v, regionWidth, regionHeight, (int)
                (width * scale), (int) (height * scale), textureWidth, textureHeight, borderWidth);
    }

    /**
     * Draws a texture with an edge around its border. The texture can be limited to a portion of itself by specifying the
     * coordinates (u, v) and (u2, regionHeight).
     * This is useful if a texture might be resized in the UI to still align its Edges
     *
     * @param drawContext   The drawContext used.
     * @param texture       The texture rendered.
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
    public static void drawTextureWithEdge(DrawContext drawContext, Identifier texture, int x, int y, int u, int v, int regionWidth, int regionHeight, int width, int height, int textureWidth, int textureHeight, int borderWidth) {
        //Center
        drawContext.drawTexture(texture, x + borderWidth, y + borderWidth, width - 2 * borderWidth, height - 2 * borderWidth, u + borderWidth, v + borderWidth, regionWidth - borderWidth * 2, regionHeight - borderWidth * 2, textureWidth, textureHeight);
        //Top Left Corner
        drawContext.drawTexture(texture, x, y, borderWidth, borderWidth, u, v, borderWidth, borderWidth, textureWidth, textureHeight);
        //Top Right Corner
        drawContext.drawTexture(texture, x + width - borderWidth, y, borderWidth, borderWidth, u + regionWidth - borderWidth, v, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Left Corner
        drawContext.drawTexture(texture, x, y + height - borderWidth, borderWidth, borderWidth, u, v + regionHeight - borderWidth, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Right Corner
        drawContext.drawTexture(texture, x + width - borderWidth, y + height - borderWidth, borderWidth, borderWidth, u + regionWidth - borderWidth, v + regionHeight - borderWidth, borderWidth, borderWidth, textureWidth, textureHeight);
        //Bottom Bar
        drawContext.drawTexture(texture, x + borderWidth, y + height - borderWidth, width - 2 * borderWidth, borderWidth, u + borderWidth, v + regionHeight - borderWidth, regionWidth - borderWidth * 2, borderWidth, textureWidth, textureHeight);
        //Right Bar
        drawContext.drawTexture(texture, x + width - borderWidth, y + borderWidth, borderWidth, height - 2 * borderWidth, u + regionWidth - borderWidth, v + borderWidth, borderWidth, regionHeight - borderWidth * 2, textureWidth, textureHeight);
        //Left Bar
        drawContext.drawTexture(texture, x, y + borderWidth, borderWidth, height - 2 * borderWidth, u, v + borderWidth, borderWidth, regionHeight - borderWidth * 2, textureWidth, textureHeight);
        //Top Bar
        drawContext.drawTexture(texture, x + borderWidth, y, width - 2 * borderWidth, borderWidth, u + borderWidth, v, regionWidth - borderWidth * 2, borderWidth, textureWidth, textureHeight);
    }


    /**
     * Draws a texture with an edge around its border. The texture can be limited to a portion of itself by specifying the
     * coordinates (u, v) and (u2, v2).
     * This is useful if a texture might be resized in the UI to still align its Edges
     *
     * @param drawContext   The drawContext used.
     * @param texture       The texture rendered.
     * @param x             The x-coordinate to draw the texture at.
     * @param y             The y-coordinate to draw the texture at.
     * @param width         The width of the texture.
     * @param height        The height of the texture.
     * @param textureWidth  The width of the texture sheet.
     * @param textureHeight The height of the texture sheet.
     * @param borderWidth   The width of the border to draw around the texture.
     */
    public static void drawTextureWithEdge(DrawContext drawContext, Identifier texture, int x, int y, int width, int height, int textureWidth, int textureHeight, int borderWidth) {
        drawTextureWithEdge(drawContext, texture, x, y, 0, 0, textureWidth, textureHeight, width, height, textureWidth, textureHeight, borderWidth);
    }

    public List<InteractAbleWidget> getHoverElements() {
        List<InteractAbleWidget> allHoverElements = new ArrayList<>(hoverElements);
        children().forEach(currentChildren -> {
            if (currentChildren instanceof InteractAbleWidget widget) {
                allHoverElements.addAll(widget.getHoverElements());
            }
        });
        return allHoverElements;
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
     * @param element the Child to be removed
     */
    public void removeChild(Element element) {
        children().remove(element);
    }

    /**
     * This Method gives direct access to the ArrayList of Children
     */
    public List<Element> children() {
        return children;
    }

    /**
     * This function triggers whenever the mouse is Moved above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     */
    @Override
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
     * @return if this consumes the Click, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public void playClickedSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @return if this consumes the Click, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseReleased(mouseX, mouseY, button)) {
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
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param amount the amount scrolled since the last time this was called
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param keyCode   the keyCode of the pressed Key
     * @param scanCode  the scanCode of the pressed Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
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
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
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
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
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
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    public Vector2d getScaledMouseCoords() {
        MinecraftClient client = MinecraftClient.getInstance();
        Mouse mouse = client.mouse;
        double x = mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double y = mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();

        return new Vector2d(x, y);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * This functions handles the Rendering
     * If you have children you should call super.render(matrices ,mouseX ,mouseY ,delta) at the end to render your children
     *
     * @param drawContext the current MatrixStack / PoseStack
     * @param mouseX      current mouseX Position
     * @param mouseY      current mouseY Position
     * @param delta       the deltaTime between frames
     *                    This is needed for animations and co
     */
    @Override
    public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if ((debug || MiapiConfig.INSTANCE.server.other.developmentMode) && Screen.hasAltDown())
            drawSquareBorder(drawContext, getX(), getY(), getWidth(), getHeight(), 1, randomColor);

        RenderSystem.setShader(GameRenderer::getPositionProgram);
        List<Element> reverse = new ArrayList<>(children());
        Collections.reverse(reverse);

        for (Element element : reverse) {
            if (element instanceof Drawable drawable) {
                drawable.render(drawContext, mouseX, mouseY, delta);
            }
        }
    }

    /**
     * This functions handles the Rendering
     * If you have children you should call super.render(matrices ,mouseX ,mouseY ,delta) at the end to render your children
     *
     * @param drawContext the current MatrixStack / PoseStack
     * @param mouseX      current mouseX Position
     * @param mouseY      current mouseY Position
     * @param delta       the deltaTime between frames
     *                    This is needed for animations and co
     */
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        hoverElements.forEach(widget -> {
            widget.render(drawContext, mouseX, mouseY, delta);
        });
        children().forEach(element -> {
            if (element instanceof InteractAbleWidget widget) {
                widget.renderHover(drawContext, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
