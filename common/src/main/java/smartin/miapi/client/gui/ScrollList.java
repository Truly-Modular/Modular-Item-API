package smartin.miapi.client.gui;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;

import java.util.List;

public class ScrollList extends InteractAbleWidget {
    private List<InteractAbleWidget> widgets;
    private int scrollAmount;
    private int maxScrollAmount;
    /**
     * Save mode is for Elements that might resize themselves on render() calls,
     * it just calls render() twice to get its height first
     */
    public boolean saveMode = true;
    boolean needsScrollbar = false;

    public ScrollList(int x, int y, int width, int height, List<InteractAbleWidget> widgets) {
        super(x, y, width, height, Text.empty());
        this.widgets = null;
        this.scrollAmount = 0;
        this.maxScrollAmount = 0;
        setList(widgets);
    }

    public void setList(List<InteractAbleWidget> widgets) {
        this.widgets = widgets;
        this.children().clear();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int totalHeight = 0;
        for (ClickableWidget widget : this.widgets) {
            totalHeight += widget.getHeight();
        }
        this.maxScrollAmount = Math.max(0, totalHeight - this.height);
        if (this.widgets == null) {
            return;
        }

        this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, this.maxScrollAmount));

        needsScrollbar = totalHeight>height;

        int startY = this.y + 1 - this.scrollAmount;

        for (ClickableWidget widget : this.widgets) {
            if (startY + widget.getHeight() >= this.y && startY <= this.y + this.height - 1) {
                widget.y = startY;
                widget.x = this.x;
                if(needsScrollbar){
                    widget.setWidth(this.width-5);
                }
                else{
                    widget.setWidth(this.width);
                }
                enableScissor(this.x,this.y,this.x+this.width,this.y+height);
                widget.render(matrices,mouseX, mouseY, delta);
                disableScissor();
            }
            startY += widget.getHeight();
        }

        if (needsScrollbar) {
            int barHeight = Math.max(10, this.height * this.height / (this.maxScrollAmount + this.height));
            int barY = this.y + 1 + (int) ((this.height - barHeight - 2) * (float) this.scrollAmount / this.maxScrollAmount);
            int barX = this.x + this.width - 5;
            fill(matrices,barX , y, barX + 5, this.y+this.height, 0xFFCCCCCC);
            fill(matrices,barX, barY, barX + 5, barY + barHeight, ColorHelper.Argb.getArgb(255,50,50,50));
        }

        if(saveMode){
            totalHeight = 0;
            for (ClickableWidget widget : this.widgets) {
                totalHeight += widget.getHeight();
            }
            this.maxScrollAmount = Math.max(0, totalHeight - this.height);
            if (this.widgets == null) {
                return;
            }

            this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, this.maxScrollAmount));

            needsScrollbar = totalHeight>height;

            startY = this.y + 1 - this.scrollAmount;

            for (ClickableWidget widget : this.widgets) {
                if (startY + widget.getHeight() >= this.y && startY <= this.y + this.height - 1) {
                    widget.y = startY;
                    widget.x = this.x;
                    if(needsScrollbar){
                        widget.setWidth(this.width-5);
                    }
                    else{
                        widget.setWidth(this.width);
                    }
                    enableScissor(this.x,this.y,this.x+this.width,this.y+height);
                    widget.render(matrices,mouseX, mouseY, 0);
                    disableScissor();
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

        int scrollSpeed = 10;

        this.scrollAmount -= amount * scrollSpeed;

        return true;
    }

    public void setScrollAmount(int amount){
        scrollAmount = amount;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isMouseOver(mouseX,mouseY)){
            if (this.widgets == null) {
                return false;
            }

            boolean clicked = false;
            if(needsScrollbar){
                if(isMouseOver(mouseX,mouseY)){
                    if(mouseY>this.x+this.width-5 && mouseY<this.x+this.width){
                        //drag motion
                    }
                }
            }

            for (ClickableWidget widget : this.widgets) {
                if(widget.isMouseOver(mouseX,mouseY)){
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
            double x = this.x;
            double y = this.y;
            double width = this.width;
            double height = this.height;
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }
    }
}

