package smartin.miapi.client.gui.crafting.crafter.glint;

import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.GlintProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GlintEditView extends InteractAbleWidget {
    public static final ResourceLocation TEXTURE = CraftingScreen.BACKGROUND_TEXTURE;

    private final ScrollList scrollList;
    List<InteractAbleWidget> colorButtons = new ArrayList<>();
    Consumer<List<Color>> preview;
    AlphaSlider speedSlider;
    float rainbowSpeed;
    EditOption.EditContext option;
    FloatWidgetField floatWidgetField;
    static float maxSpeed = 4.0f;

    public GlintEditView(int x, int y, int width, int height, EditOption.EditContext editContext, Consumer<GlintProperty.RainbowGlintSettings> onChange, Consumer<GlintProperty.RainbowGlintSettings> onCraft) {
        super(x, y, width, height, Component.empty());
        option = editContext;
        GlintProperty.RainbowGlintSettings oldSettings = GlintProperty.property.getData(editContext.getItemstack()).orElse(GlintProperty.defaultSettings);
        this.preview = (list -> {
            onChange.accept(glintSettings(oldSettings, list));
        });

        // Initialize color list, ensure at least one transparent color if empty;

        List<Color> colors = new ArrayList<>(Arrays.stream(oldSettings.colors).toList());
        if (colors.isEmpty()) {
            colors.add(Color.RED); // Transparent color
        }

        // Create scrollable list for colors
        List<InteractAbleWidget> widgets = new ArrayList<>();
        this.scrollList = new ScrollList(getX(), getY(), getWidth(), getHeight() - 20, widgets); // Adjust height for buttons
        this.addChild(scrollList);
        this.scrollList.alwaysEnableScrollbar = true;
        floatWidgetField = new FloatWidgetField(Minecraft.getInstance().font, getX() + 60, getY() + getHeight() - 13, 30, 10, 0.0f, 128.0f, Component.empty(), (f) -> {
            updateSpeed(onChange, f, oldSettings);
            speedSlider.value = Math.clamp(f / maxSpeed, 0, 1);
        }, "");
        this.addChild(floatWidgetField);

        // Populate scroll list with initial colors
        SimpleButton<Void> plusButton2 = new SimpleButton<>(0, 0, 50, 10, Component.literal("+"), null, callback -> {
            Color newColor = Color.RED;
            addColorToScrollList(newColor);
            this.preview.accept(getColors());
        });
        colorButtons.add(plusButton2);
        for (Color color : colors) {
            addColorToScrollList(color);
        }
        scrollList.setList(colorButtons);


        speedSlider = new AlphaSlider(getX() + 3, getY() + getHeight() - 12, 50, 6, (f) -> {
            updateSpeed(onChange, f * maxSpeed, oldSettings);
            this.floatWidgetField.setValue(f * maxSpeed);
        });
        this.floatWidgetField.setValue(oldSettings.rainbowSpeed);
        speedSlider.value = Math.clamp(oldSettings.rainbowSpeed / maxSpeed, 0, 1);
        this.addChild(speedSlider);

        // Add "Apply" button
        SimpleButton<Void> applyButton = new SimpleButton<>(getX() + getWidth() - 50, getY() + getHeight() - 18, 40, 16, Component.literal("Apply"), null, callback -> {
            // Apply button logic can be added later
            onCraft.accept(glintSettings(oldSettings, getColors()));
        });
        this.addChild(applyButton);
    }

    private void updateSpeed(Consumer<GlintProperty.RainbowGlintSettings> onChange, Float f, GlintProperty.RainbowGlintSettings oldSettings) {
        GlintProperty.RainbowGlintSettings settings = glintSettings(oldSettings, getColors());
        settings.rainbowSpeed = f;
        this.rainbowSpeed = settings.rainbowSpeed;
        onChange.accept(settings);
    }

    public List<Color> getColors() {
        return colorButtons.stream().filter(SingleColorEdit.class::isInstance).map(a -> ((SingleColorEdit) a).currentColor).toList();
    }

    private void addColorToScrollList(Color color) {
        SingleColorEdit singleColorEdit = new SingleColorEdit(0, 0, getWidth() - 20, Component.literal("Color"), color, (button, updatedColor) -> {
            int index = colorButtons.indexOf(button);
            if (index != -1) {
                this.preview.accept(getColors());
            }
        });
        colorButtons.add(Math.max(0, scrollList.widgets.size() - 1), singleColorEdit);
        scrollList.setList(colorButtons);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public GlintProperty.RainbowGlintSettings glintSettings(GlintProperty.RainbowGlintSettings oldSettings, List<Color> list) {
        oldSettings = oldSettings.copyWithColor(list);
        oldSettings.rainbowSpeed = rainbowSpeed;
        oldSettings.isItem = option.getInstance() == null;

        return oldSettings;
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.blit(TEXTURE, getX(), getY() + getHeight() - 18, 0, 0, 295 + 92, 92, 16, 512, 512);
        super.renderWidget(drawContext, mouseX, mouseY, delta);
        if ((debug || MiapiConfig.INSTANCE.server.other.developmentMode) && Screen.hasAltDown())
            drawSquareBorder(drawContext, getX(), getY(), getWidth(), getHeight(), 1, randomColor);
    }

    /**
     * This Widget is a single color entry, able to customize it.
     */
    protected class SingleColorEdit extends InteractAbleWidget {
        private final ColorPickerWidget colorPicker;
        private final HueSlider hueSlider;
        private final SimpleButton<Void> removeButton;
        private final SimpleButton<Void> toggleButton;
        private boolean isExpanded;
        public Color currentColor;

        public SingleColorEdit(int x, int y, int width, Component title, Color initialColor, BiConsumer<SingleColorEdit, Color> onColorChange) {
            super(x, y, width, 13, title);

            this.isExpanded = false;
            initialColor.setAlpha(255);
            currentColor = initialColor;

            // Create color picker widget (initially hidden)
            this.colorPicker = new ColorPickerWidget(Minecraft.getInstance().font, x + 10, y + 25, selectedColor -> {
                currentColor = selectedColor;
                onColorChange.accept(this, selectedColor);
            });
            this.colorPicker.setColor(initialColor);
            this.colorPicker.visible = false;

            // Add remove button
            this.removeButton = new SimpleButton<>(x + width - 3, y + 1, 11, 11, Component.literal("X"), null, callback -> {
                colorButtons.remove(this);
                scrollList.setList(colorButtons);
            });
            this.addChild(removeButton);

            // Add toggle button to show/hide color picker
            this.toggleButton = new SimpleButton<>(x + width - 16, y + 1, 11, 11, Component.literal("..."), null, callback -> {
                isExpanded = !isExpanded;
                this.colorPicker.visible = isExpanded;
                this.setHeight(isExpanded ? 13 + colorPicker.getHeight() : 13); // Adjust height based on expanded state
                if (isExpanded) {
                    this.addChild(colorPicker);
                } else {
                    this.removeChild(colorPicker);
                }
            });
            this.hueSlider = new HueSlider(x + 16, y + 3, x + getWidth() - 30, 8, (a) -> {
                colorPicker.setColorAndUpdate(a, colorPicker.colorGrid.saturation, colorPicker.colorGrid.lightness);
            });
            float[] hsl = MathUtil.rgbToHsv(currentColor.r(), currentColor.g(), currentColor.b());
            hueSlider.value = MathUtil.clamp(hsl[0] / 360, 0, 1);
            this.addChild(hueSlider);
            this.addChild(toggleButton);
        }

        @Override
        public void setX(int x) {
            this.removeButton.setX(x + width - 3 - 8);
            this.toggleButton.setX(x + width - 16 - 8);
            this.colorPicker.setX(x + 5);
            this.hueSlider.setX(x + 16);
            super.setX(x);
        }

        @Override
        public void setY(int y) {
            this.removeButton.setY(y);
            this.toggleButton.setY(y);
            this.colorPicker.setY(y + 12);
            this.hueSlider.setY(y + 3);
            super.setY(y);
        }

        @Override
        public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
            // Render the color display box
            drawContext.fill(getX() + 1, getY() + 1, getX() + 10, getY() + 10, currentColor.argb());
            super.renderWidget(drawContext, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (GuiEventListener child : this.children()) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }
    }
}
