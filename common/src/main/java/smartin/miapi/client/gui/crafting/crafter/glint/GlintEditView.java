package smartin.miapi.client.gui.crafting.crafter.glint;

import com.redpxnda.nucleus.config.screen.widget.colorpicker.ColorPickerWidget;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.modules.edit_options.EditOption;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlintEditView extends InteractAbleWidget {
    private final EditOption.EditContext editContext;
    private final ScrollList scrollList;
    private final Consumer<List<Color>> onChange;
    private final List<Color> colors;

    public GlintEditView(int x, int y, int width, int height, EditOption.EditContext editContext, Consumer<List<Color>> onChange) {
        super(x, y, width, height, Component.empty());

        this.editContext = editContext;
        this.onChange = onChange;

        // Initialize color list, ensure at least one transparent color if empty
        this.colors = new ArrayList<>();
        if (this.colors.isEmpty()) {
            this.colors.add(new Color(0, 0, 0, 0)); // Transparent color
        }

        // Create scrollable list for colors
        List<InteractAbleWidget> widgets = new ArrayList<>();
        this.scrollList = new ScrollList(getX(), getY(), getWidth(), getHeight() - 50, widgets); // Adjust height for buttons
        this.addChild(scrollList);

        // Populate scroll list with initial colors
        for (Color color : colors) {
            addColorToScrollList(color);
        }

        // Add "Plus" button to add more colors
        SimpleButton<Void> plusButton = new SimpleButton<>(getX() + 10, getY() + getHeight() - 40, 50, 20, Component.literal("+"), null, callback -> {
            Color newColor = Color.WHITE; // Default white color
            colors.add(newColor);
            addColorToScrollList(newColor);
            onChange.accept(colors);
        });
        this.addChild(plusButton);

        // Add "Apply" button
        SimpleButton<Void> applyButton = new SimpleButton<>(getX() + getWidth() - 70, getY() + getHeight() - 40, 60, 20, Component.literal("Apply"), null, callback -> {
            // Apply button logic can be added later
        });
        this.addChild(applyButton);
    }

    private void addColorToScrollList(Color color) {
        SingleColorEdit singleColorEdit = new SingleColorEdit(0, 0, getWidth() - 20, 30, Component.literal("Color"), color, updatedColor -> {
            int index = colors.indexOf(color);
            if (index != -1) {
                colors.set(index, updatedColor);
                onChange.accept(colors);
            }
        });
        scrollList.addChild(singleColorEdit);
    }

    /**
     * This Widget is a single color entry, able to customize it.
     */
    protected class SingleColorEdit extends InteractAbleWidget {
        private final ColorPickerWidget colorPicker;
        private final SimpleButton<Void> removeButton;
        private final SimpleButton<Void> toggleButton;
        private boolean isExpanded;
        Color currentColor;

        public SingleColorEdit(int x, int y, int width, int height, Component title, Color initialColor, Consumer<Color> onColorChange) {
            super(x, y, width, height, title);

            this.isExpanded = false;
            currentColor = initialColor;

            // Create color picker widget (initially hidden)
            this.colorPicker = new ColorPickerWidget(Minecraft.getInstance().font, x + 10, y + 25, selectedColor -> {
                onColorChange.accept(selectedColor);
                currentColor = selectedColor;
            });
            this.colorPicker.setColor(initialColor);
            this.colorPicker.visible = false;
            this.addChild(colorPicker);

            // Add remove button
            this.removeButton = new SimpleButton<>(x + width - 25, y + 5, 20, 20, Component.literal("X"), null, callback -> {
                scrollList.removeChild(this);
            });
            this.addChild(removeButton);

            // Add toggle button to show/hide color picker
            this.toggleButton = new SimpleButton<>(x + width - 50, y + 5, 20, 20, Component.literal("..."), null, callback -> {
                isExpanded = !isExpanded;
                this.colorPicker.visible = isExpanded;
                this.setHeight(isExpanded ? 80 : 30); // Adjust height based on expanded state
            });
            this.addChild(toggleButton);
        }

        @Override
        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            // Render the color display box
            drawContext.fill(getX() + 5, getY() + 5, getX() + 25, getY() + 25,  currentColor.argb());
            super.renderWidget(drawContext, mouseX, mouseY, delta);
        }
    }
}
