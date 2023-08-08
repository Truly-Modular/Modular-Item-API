package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.*;
import smartin.miapi.modules.properties.MiningLevelProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class MiningLevelStatDisplay extends InteractAbleWidget implements SingleStatDisplay {
    public Identifier texture = new Identifier("textures/gui/container/inventory.png");
    public ItemStack original = ItemStack.EMPTY;
    public ItemStack compareTo = ItemStack.EMPTY;
    public StatBar statBar;
    public ScrollingTextWidget currentValue;
    public ScrollingTextWidget compareValue;
    public ScrollingTextWidget centerValue;
    public ScrollingTextWidget textWidget;
    public double maxValue = 100;
    public double minValue = 0;

    public int maxValueInt = 4;
    public int minValueInt = 0;
    public DecimalFormat modifierFormat;
    public StatDisplay.TextGetter text;
    public StatDisplay.TextGetter hover;
    public HoverDescription hoverDescription;
    public IntegerStatBar integerStatBar;
    public String type;
    EntityAttribute attribute;
    double defaultValue;
    EquipmentSlot slot;

    public MiningLevelStatDisplay(String type, StatDisplay.TextGetter title, StatDisplay.TextGetter hover) {
        super(0, 0, 80, 32, Text.empty());
        this.type = type;
        text = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(getX(), getY(), 80, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(getX(), getY(), 50, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(getX(), getY(), 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
        compareValue = new ScrollingTextWidget(getX(), getY(), 80 - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
        statBar = new StatBar(0, 0, width, 10, ColorHelper.Argb.getArgb(255, 0, 0, 0));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(getX(), getY(), List.of());
        integerStatBar = new IntegerStatBar(0, 0, width, 1);
        integerStatBar.setGapWidth(2);
        integerStatBar.setHeight(10);
        integerStatBar.setMaxSteps(maxValueInt);
    }

    public double getValue(ItemStack stack) {
        return AttributeRegistry.getAttribute(stack, attribute, slot, defaultValue);
    }

    public int getAltValue(ItemStack stack) {
        return MiningLevelProperty.getMiningLevel(type, stack);
    }

    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        textWidget.setText(text.resolve(mainStack));
        hoverDescription.setText(hover.resolve(mainStack));
        this.original = original;
        this.compareTo = compareTo;
        if (original.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        if (compareTo.getAttributeModifiers(slot).containsKey(attribute)) {
            return true;
        }
        return getAltValue(original) > 0 || getAltValue(compareTo) > 0;
    }

    @Override
    public InteractAbleWidget getHoverWidget() {
        return hoverDescription;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        double oldValue = getValue(original);
        double compareToValue = getValue(compareTo);
        int oldMining = getAltValue(original);
        int compareMining = getAltValue(compareTo);

        double min = Math.min(minValue, Math.min(oldValue, compareToValue));
        double max = Math.max(maxValue, Math.max(oldValue, compareToValue));

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 0, 166, 120, 32, width, height, 256, 256, 2);

        textWidget.setX(this.getX() + 5);
        textWidget.setY(this.getY() + 4);
        textWidget.setWidth(this.width - 8);

        statBar.setX(this.getX() + 5);
        statBar.setY(this.getY() + 22);
        statBar.setWidth(this.width - 10);
        statBar.setHeight(2);
        integerStatBar.setX(this.getX() + 5);
        integerStatBar.setY(this.getY() + 25);
        integerStatBar.setWidth(this.width - 10);
        integerStatBar.setHeight(2);
        String centerText = "";
        if (oldMining < compareMining) {
            integerStatBar.setPrimary(oldMining, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            integerStatBar.setSecondary(compareMining, ColorHelper.Argb.getArgb(255, 0, 255, 0));
        } else {
            integerStatBar.setPrimary(compareMining, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            integerStatBar.setSecondary(oldMining, ColorHelper.Argb.getArgb(255, 255, 0, 0));
        }
        if (oldValue < compareToValue) {
            statBar.setPrimary((oldValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((compareToValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 0, 255, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 0, 255, 0);
        } else {
            statBar.setPrimary((compareToValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 255, 255));
            statBar.setSecondary((oldValue - min) / (max - min), ColorHelper.Argb.getArgb(255, 255, 0, 0));
            compareValue.textColor = ColorHelper.Argb.getArgb(255, 255, 0, 0);
        }
        if (oldValue == compareToValue) {
            compareValue.textColor = ColorHelper.Argb.getArgb(0, 255, 0, 0);
        } else {
            compareValue.setX(this.getX() + 5);
            compareValue.setY(this.getY() + 13);
            compareValue.setText(Text.of(modifierFormat.format(compareToValue)));
            compareValue.render(drawContext, mouseX, mouseY, delta);
            centerText = "→";
        }
        currentValue.setX(this.getX() + 5);
        currentValue.setY(this.getY() + 13);
        currentValue.setText(Text.literal(modifierFormat.format(oldValue)));
        currentValue.render(drawContext, mouseX, mouseY, delta);
        centerValue.setX(this.getX() + 5);
        centerValue.setY(this.getY() + 13);
        centerValue.setText(Text.literal(centerText));
        centerValue.render(drawContext, mouseX, mouseY, delta);
        statBar.render(drawContext, mouseX, mouseY, delta);
        integerStatBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    public static Builder Builder(String type) {
        return new Builder(type);
    }

    public static class Builder {
        public StatDisplay.TextGetter name;
        public StatDisplay.TextGetter hoverDescription = (stack) -> Text.empty();
        EntityAttribute attribute;
        public EquipmentSlot slot = EquipmentSlot.MAINHAND;
        public double defaultValue = 1;
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 12;
        public String type;

        private Builder(String type) {
            this.type = type;
            setTranslationKey("mining.level." + type);
            modifierFormat = Util.make(new DecimalFormat("##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setAttribute(EntityAttribute attribute) {
            this.attribute = attribute;
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public Builder setSlot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder setDefault(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setName(StatDisplay.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public Builder setHoverDescription(Text hoverDescription) {
            this.hoverDescription = (stack) -> hoverDescription;
            return this;
        }

        public Builder setHoverDescription(StatDisplay.TextGetter hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
            return this;
        }

        public MiningLevelStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }

            // Create an instance of AttributeProperty with the builder values
            MiningLevelStatDisplay display = new MiningLevelStatDisplay(type, name, hoverDescription);
            display.attribute = attribute;
            display.slot = slot;
            display.defaultValue = defaultValue;
            display.minValue = min;
            display.maxValue = max;
            display.modifierFormat = modifierFormat;
            return display;
        }
    }
}
