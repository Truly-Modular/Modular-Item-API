package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

/**
 * This class represents a scrollable list of interactive widgets.
 * It extends the InteractAbleWidget class and provides methods for
 * setting the list of widgets, rendering the list, scrolling the list,
 * and handling mouse events.
 */
@Environment(EnvType.CLIENT)
public class ScrollList extends InteractAbleWidget {
    private List<InteractAbleWidget> widgets;
    private int scrollAmount;
    private int maxScrollAmount;
    public boolean saveMode = true;
    boolean needsScrollbar = false;

    /**
     * Constructs a new ScrollList with the given x and y coordinates, width, height,
     * and list of InteractAbleWidgets.
     *
     * @param x       the x-coordinate of the ScrollList
     * @param y       the y-coordinate of the ScrollList
     * @param width   the width of the ScrollList
     * @param height  the height of the ScrollList
     * @param widgets the list of InteractAbleWidgets to display in the ScrollList
     */
    public ScrollList(int x, int y, int width, int height, List<InteractAbleWidget> widgets) {
        super(x, y, width, height, Text.empty());
        this.widgets = null;
        this.scrollAmount = 0;
        this.maxScrollAmount = 0;
        setList(widgets);
    }

    /**
     * Sets the list of InteractAbleWidgets to be displayed in the ScrollList.
     *
     * @param widgets the list of InteractAbleWidgets to display in the ScrollList
     */
    public void setList(List<InteractAbleWidget> widgets) {
        this.widgets = widgets;
        this.children().clear();
    }

    /**
     * Sets the amount of scrolling for this ScrollList.
     *
     * @param amount the amount of scrolling to set.
     */
    public void setScrollAmount(int amount) {
        scrollAmount = amount;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        int totalHeight = 0;
        for (ClickableWidget widget : this.widgets) {
            totalHeight += widget.getHeight();
        }
        this.maxScrollAmount = Math.max(0, totalHeight - this.height);
        if (this.widgets == null) {
            return;
        }

        this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, this.maxScrollAmount));

        needsScrollbar = totalHeight > height;

        int startY = this.getY() + 1 - this.scrollAmount;

        for (ClickableWidget widget : this.widgets) {
            if (startY + widget.getHeight() >= this.getY() && startY <= this.getY() + this.height - 1) {
                widget.setY(startY);
                widget.setX(this.getX());
                if (needsScrollbar) {
                    widget.setWidth(this.width - 5);
                } else {
                    widget.setWidth(this.width);
                }
                Matrix4f matrix4f = new Matrix4f(drawContext.getMatrices().peek().getPositionMatrix());
                Vector4f posX = TransformableWidget.transFormMousePos(getX(), getY(), matrix4f);
                Vector4f posY = TransformableWidget.transFormMousePos(getX() + width, getY() + height, matrix4f);

                drawContext.enableScissor((int) posX.x(), (int) posX.y, (int) posY.x(), (int) posY.y());
                widget.render(drawContext, mouseX, mouseY, delta);
                drawContext.disableScissor();
            }
            startY += widget.getHeight();
        }

        if (needsScrollbar) {
            int barHeight = Math.max(10, this.height * this.height / (this.maxScrollAmount + this.height));
            int barY = this.getY() + 1 + (int) ((this.height - barHeight - 2) * (float) this.scrollAmount / this.maxScrollAmount);
            int barX = this.getX() + this.width - 5;
            drawContext.fill(barX, getY(), barX + 5, this.getY() + this.height, 0xFFCCCCCC);
            drawContext.fill(barX, barY, barX + 5, barY + barHeight, ColorHelper.Argb.getArgb(255, 50, 50, 50));
        }
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            for (InteractAbleWidget widget : this.widgets) {
                widget.renderHover(drawContext, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.widgets == null) {
            return false;
        }
        for (ClickableWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }

        int scrollSpeed = 10;

        this.scrollAmount -= amount * scrollSpeed;

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (widgets != null) {
            for (InteractAbleWidget widget : widgets) {
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (widgets != null) {
            for (InteractAbleWidget widget : widgets) {
                if (widget.keyReleased(keyCode, scanCode, modifiers))
                    return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            if (this.widgets == null) {
                return false;
            }

            boolean clicked = false;
            if (needsScrollbar) {
                if (isMouseOver(mouseX, mouseY)) {
                    if (mouseY > this.getX() + this.width - 5 && mouseY < this.getX() + this.width) {
                        //TODO:drag motion?
                    }
                }
            }

            for (ClickableWidget widget : this.widgets) {
                if (widget.isMouseOver(mouseX, mouseY)) {
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.widgets == null) {
            return false;
        }

        boolean released = false;

        for (ClickableWidget widget : this.widgets) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                released = true;
            }
        }

        return released;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!this.visible) {
            return false;
        } else {
            double x = this.getX();
            double y = this.getY();
            double width = this.width;
            double height = this.height;
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }
}

