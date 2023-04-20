package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

public abstract class ParentHandledScreen<T extends ScreenHandler> extends HandledScreen<T> {

    /**
     * This is a Handled Screen class that by default correctly handles Children.
     * If you want those Children to support Children the use of the InteractAbleWidget class is recommended
     * @param handler the ScreenHandler linked to this HandledScreen
     * @param inventory the PlayerInventory of the Player opening this screen
     * @param title the Title of the Screen
     */
    public ParentHandledScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super((T) handler, inventory, title);
    }

    /**
     * This Method adds a Child to this Widget
     * native Access to the Array is given in children()
     * @param element the Child to be Added
     */
    public void addChild(ClickableWidget element) {
        addSelectableChild(element);
    }

    /**
     * This Method removes a child Element from this Widget
     * native Access to the Array is given in children()
     * @param element
     */
    public void removeChild(Element element){
        children().remove(element);
    }

    /**
     * This function triggers whenever the mouse is Moved above the Widget
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
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @return if this consumes the Click, if you execute an action return true, if not return false
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     *
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
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     *
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
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     *
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
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    /**
     *
     * @param keyCode the keyCode of the pressed Key
     * @param scanCode the scanCode of the pressed Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element child : this.children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     *
     * @param keyCode the keyCode of the released Key
     * @param scanCode the scanCode of the released Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Element child : this.children()) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /**
     *
     * @param chr the Character typed
     * @param modifiers if keys like ctrl or alt where held down
     * @return if this consumes the action, if you execute an action return true, if not return false
     */
    public boolean charTyped(char chr, int modifiers) {
        for (Element child : this.children()) {
            if (child.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    /**
     * This Function handles if the Mouse is above the current Widget
     * Its not recommended to adjust this if your widget is a Rectangle,
     * if it is not make sure this function returns true whenever the mouse is above the widget
     * @param mouseX current mouseX coordinate
     * @param mouseY current mouseY coordinate
     * @return if the mouseCords are above the Widget
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    /**
     * This functions handles the Rendering
     * If you have children you should call super.render(matrices ,mouseX ,mouseY ,delta) at the end to render your children
     * @param matrices the current MatrixStack / PoseStack
     * @param mouseX current mouseX Position
     * @param mouseY current mouseY Position
     * @param delta the deltaTime between frames
     *              This is needed for animations and co
     */
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
        super.render(matrices,mouseX,mouseY,delta);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();
        children().forEach(element -> {
            if(element instanceof Drawable drawable){
                drawable.render(matrices,mouseX,mouseY,delta);
            }
        });
    }
}
