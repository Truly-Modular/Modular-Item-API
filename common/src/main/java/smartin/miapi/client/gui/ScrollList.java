package smartin.miapi.client.gui;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

import java.util.List;

public class ScrollList extends InteractAbleWidget {
    private List<InteractAbleWidget> widgets;
    private int scrollAmount;
    private int maxScrollAmount;

    public ScrollList(int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
        this.widgets = null;
        this.scrollAmount = 0;
        this.maxScrollAmount = 0;
    }

    public void setList(List<InteractAbleWidget> widgets) {
        this.widgets = widgets;
        this.children().clear();
        //this.children().addAll(widgets);
        updateMaxScrollAmount();
    }

    private void updateMaxScrollAmount() {
        int totalHeight = 0;
        for (ClickableWidget widget : this.widgets) {
            totalHeight += widget.getHeight();
        }
        this.maxScrollAmount = Math.max(0, totalHeight - this.height);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Miapi.LOGGER.error("renderingList"+widgets.size());
        if (this.widgets == null) {
            return;
        }

        this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, this.maxScrollAmount));

        int startY = this.y + 1 - this.scrollAmount;

        for (ClickableWidget widget : this.widgets) {
            if (startY + widget.getHeight() >= this.y && startY <= this.y + this.height - 1) {
                widget.y = startY;
                widget.x = this.x;
                widget.render(matrices,mouseX, mouseY, delta);
                Miapi.LOGGER.warn("rendering widget"+widget.getMessage());
            }
            startY += widget.getHeight();
        }

        if (this.maxScrollAmount > 0) {
            int barHeight = Math.max(10, this.height * this.height / (this.maxScrollAmount + this.height));
            int barY = this.y + 1 + (int) ((this.height - barHeight - 2) * (float) this.scrollAmount / this.maxScrollAmount);
            int barX = this.x + this.width - 6;
            fill(matrices,barX, barY, barX + 5, barY + barHeight, 0xFFCCCCCC);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.widgets == null) {
            return false;
        }

        int scrollSpeed = 10;

        this.scrollAmount -= amount * scrollSpeed;

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.widgets == null) {
            return false;
        }

        boolean clicked = false;

        for (ClickableWidget widget : this.widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                clicked = true;
            }
        }

        return clicked;
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
            double x = this.x;
            double y = this.y;
            double width = this.width;
            double height = this.height;
            //if (this.hasScrollbar()) {
            //    width -= 6;
            //}
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }
}

