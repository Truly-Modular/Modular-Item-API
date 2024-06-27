package smartin.miapi.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;


/**
 * An abstract class that extends {@link AbstractContainerScreen} and provides default handling for children widgets.
 * This class should be extended by screens that have child widgets, and if the child widgets themselves have children,
 * it's recommended to use the {@link InteractAbleWidget} class.
 *
 * @param <T> the type of the screen handler associated with this screen
 */
public abstract class ParentHandledScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    /**
     * This is a Handled Screen class that by default correctly handles Children.
     * If you want those Children to support Children the use of the InteractAbleWidget class is recommended
     *
     * @param handler   the ScreenHandler linked to this HandledScreen
     * @param inventory the PlayerInventory of the Player opening this screen
     * @param title     the Title of the Screen
     */
    protected ParentHandledScreen(T handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    /**
     * This Method adds a Child to this Widget
     * native Access to the Array is given in children()
     *
     * @param element the Child to be Added
     */
    public void addChild(AbstractWidget element) {
        addWidget(element);
    }

    /**
     * This Method removes a child Element from this Widget
     * native Access to the Array is given in children()
     *
     * @param element the child to be removed
     */
    public void removeChild(GuiEventListener element) {
        children().remove(element);
    }

    /**
     * Acess the slot the mouse is hovering over
     *
     * @return
     */
    @Nullable
    public Slot getFocusSlot() {
        return this.hoveredSlot;
    }


    /**
     * This function triggers whenever the mouse is Moved above the Widget
     *
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     */
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (GuiEventListener child : this.children().stream().toList()) {
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
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param button the Number of the Button
     * @return if this consumes the Click, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     * @param mouseX current X Position of the Mouse
     * @param mouseY current Y Position of the Mouse
     * @param scrollY the amount scrolled Y since the last time this was called
     * @param scrollX the amount scrolled X since the last time this was called
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * @param keyCode   the keyCode of the pressed Key
     * @param scanCode  the scanCode of the pressed Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * @param keyCode   the keyCode of the released Key
     * @param scanCode  the scanCode of the released Key
     * @param modifiers if addition buttons like ctrl or alt where held down
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener child : this.children().stream().toList()) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /**
     * @param chr       the Character typed
     * @param modifiers if keys like ctrl or alt where held down
     * @return if this consumes the action, if you previewStack an action return true, if not return false
     */
    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (GuiEventListener child : this.children().stream().toList()) {
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
     *
     * @param mouseX current mouseX coordinate
     * @param mouseY current mouseY coordinate
     * @return if the mouseCords are above the Widget
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY);
    }

    /**
     * This functions handles the Rendering
     * If you have children you should call super.render(matrices ,mouseX ,mouseY ,delta) at the end to render your children
     *
     * @param context the current drawContext
     * @param mouseX  current mouseX Position
     * @param mouseY  current mouseY Position
     * @param delta   the deltaTime between frames
     *                This is needed for animations and co
     */
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        children().forEach(element -> {
            if (element instanceof Renderable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        });
        super.render(context, mouseX, mouseY, delta);
    }

    public void renderHover(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().last().pose().transform(new Vector4f(0, 0, 500, 0));
        children().forEach(element -> {
            if (element instanceof InteractAbleWidget drawable) {
                drawable.renderHover(context, mouseX, mouseY, delta);
            }
        });
    }
}
