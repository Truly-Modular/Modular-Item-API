package smartin.miapi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BoxList extends InteractAbleWidget {
    private List<List<ClickableWidget>> allRows = new ArrayList<>();
    private List<ClickableWidget> currentWidgets = new ArrayList<>();
    private int space = 5;
    public int maxPageHeight;

    /**
     * @param x       the x Position of the BoxList
     * @param y       the y Position of the BoxList
     * @param width   the width of the BoxList
     * @param height  the width of the BoxList
     * @param message the Title of the BoxList, Unused
     * @param widgets the supplier of Clickable Elements
     *                once the first number returns null
     *                no further elements are added.
     *                Make sure x,y,width and height are working for the ClickAbleWidgets
     */
    public BoxList(int x, int y, int width, int height, Text message, List<ClickableWidget> widgets) {
        super(x, y, width, height, message);
        setWidgets(widgets, space);
    }

    public void setSpace(int space) {
        this.space = space;
        List<ClickableWidget> widgets = new ArrayList<>();
        setWidgets(widgets, space);
    }

    /**
     * This functions resets the List of widgets
     *
     * @param widgets the new List of widgets
     * @param space   the space in pixels between the widgets
     */
    public void setWidgets(@Nullable List<ClickableWidget> widgets, int space) {
        this.children().clear();
        this.space = space;
        if (widgets == null) {
            widgets = new ArrayList<>();
        }
        currentWidgets = widgets;

        HashMap<Integer, List<ClickableWidget>> byHeight = new LinkedHashMap<>();
        widgets.forEach(widget -> {
            byHeight.putIfAbsent(widget.getHeight(), new ArrayList<>());
            byHeight.get(widget.getHeight()).add(widget);
        });
        int currentHeight = this.getY();
        while (!byHeight.isEmpty()) {
            List<ClickableWidget> toAdd = byHeight.remove(byHeight.keySet().stream().findAny().get());

            toAdd.sort(Comparator.comparingInt(ClickableWidget::getHeight));

            while (!toAdd.isEmpty()) {
                ClickableWidget widget = toAdd.remove(0);
                int currentWidth = 0;
                List<ClickableWidget> currentRow = new ArrayList<>();
                currentRow.add(widget);
                widget.setX(this.getX() + currentWidth + space);
                widget.setY(currentHeight + space);
                this.addChild(widget);
                currentWidth += widget.getWidth() + space;
                for (int i = 0; i < toAdd.size(); i++) {
                    if (currentWidth + toAdd.get(i).getWidth() < this.getWidth()) {
                        ClickableWidget next = toAdd.remove(i);
                        next.setX(this.getX() + currentWidth + space);
                        next.setY(currentHeight + space);
                        currentWidth += this.width + space;
                        this.addChild(next);
                    }
                }
                currentHeight += widget.getHeight() + space;
                allRows.add(currentRow);
            }
        }
        this.height = currentHeight - this.getY();
    }

    @Override
    public void setX(int x) {
        if (this.getX() != x) {
            super.setX(x);
            this.setWidgets(currentWidgets, space);
        }
    }

    @Override
    public void setY(int y) {
        if (this.getY() != y) {
            super.setY(y);
            this.setWidgets(currentWidgets, space);
        }
    }

    @Override
    public void setWidth(int widths) {
        if (this.getWidth() != width) {
            super.setWidth(width);
            this.setWidgets(currentWidgets, space);
        }
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}
