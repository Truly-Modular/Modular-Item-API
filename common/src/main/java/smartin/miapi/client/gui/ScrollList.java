package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.client.gui.crafting.CraftingScreen;

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
    public boolean alwaysEnableScrollbar = false;
    int barWidth = 8;
    boolean scrollbarDragged = false;
    public boolean altDesign = false;

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

    /**
     *
     * @return the current scroll depth
     */
    public int getScrollAmount() {
        return scrollAmount;
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

        int startY = this.getY() - this.scrollAmount;

        for (ClickableWidget widget : this.widgets) {
            if (startY + widget.getHeight() >= this.getY() && startY <= this.getY() + this.height - 1) {
                widget.setY(startY);
                widget.setX(this.getX());
                if (showScrollbar()) {
                    widget.setWidth(this.width - barWidth);
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

        if (showScrollbar()) {
            int barX = this.getX() + this.width - barWidth;
            float percentage = (float) this.scrollAmount / (float) maxScrollAmount;
            renderScrollbarBackground(drawContext, mouseX, mouseY, delta, barX, barWidth);
            renderScrollbarClickAble(drawContext, mouseX, mouseY, delta, barX, barWidth, percentage);
        }
    }

    public void renderScrollbarBackground(DrawContext drawContext, int mouseX, int mouseY, float delta, int barX, int barWidth) {
        int offsetAlt = altDesign ? 28 : 0;
        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, barX, getY(), 498 - offsetAlt, 96, 14, 15, barWidth, getHeight(), 512, 512, 3);
    }

    public void renderScrollbarClickAble(DrawContext drawContext, int mouseX, int mouseY, float delta, int barX, int barWidth, float percent) {
        int height = (int) ((this.getHeight() - 17) * percent) + (altDesign ? percent >= 1 ? 2 : 0 : 1) + getY();
        int offset = needsScrollbar ? 0 : 15;
        int offsetAlt = altDesign ? 28 : 0;
        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, barX, height, 498 - 14 - offsetAlt, 96 + offset, 14, 15, barWidth, 15, 512, 512, 3);
    }

    private boolean showScrollbar() {
        return needsScrollbar || alwaysEnableScrollbar;
    }

    @Override
    public void renderHover(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            int startY = this.getY() - this.scrollAmount;
            for (ClickableWidget widget : this.widgets) {
                if (startY + widget.getHeight() >= this.getY() && startY <= this.getY() + this.height - 1) {
                    if(widget instanceof InteractAbleWidget interactAbleWidget){
                        widget.setY(startY);
                        widget.setX(this.getX());
                        if (showScrollbar()) {
                            widget.setWidth(this.width - barWidth);
                        } else {
                            widget.setWidth(this.width);
                        }
                        interactAbleWidget.renderHover(drawContext, mouseX, mouseY, delta);
                    }
                }
                startY += widget.getHeight();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.widgets == null) {
            return false;
        }
        for (ClickableWidget widget : widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, amount)) {
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
            if (showScrollbar()) {
                if (isMouseOver(mouseX, mouseY)) {
                    if (mouseX > this.getX() + this.width - barWidth && mouseX < this.getX() + this.width) {
                        scrollbarDragged = true;
                        return true;
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX > this.getX() + this.width - barWidth && mouseX < this.getX() + this.width) {
            if (mouseY < (double) this.getY()) {
                this.scrollAmount = 0;
                return true;
            } else if (mouseY > (double) (this.getY() + this.height)) {
                this.scrollAmount = maxScrollAmount;
                return true;
            } else {
                double i = Math.min(1, Math.max(0, (mouseY - getY()) / (getHeight() - 10)));
                this.scrollAmount = (int) (i * maxScrollAmount);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.widgets == null) {
            return false;
        }
        if (showScrollbar()) {
            if (isMouseOver(mouseX, mouseY)) {
                if (mouseX > this.getX() + this.width - barWidth && mouseX < this.getX() + this.width) {
                    scrollbarDragged = false;
                    return true;
                }
            }
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

