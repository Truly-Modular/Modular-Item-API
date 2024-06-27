package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class MiningLevelStatDisplay extends InteractAbleWidget implements SingleStatDisplay {
    public ResourceLocation texture = CraftingScreen.BACKGROUND_TEXTURE;
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
    public StatListWidget.TextGetter text;
    public StatListWidget.TextGetter hover;
    public HoverDescription hoverDescription;
    public IntegerStatBar integerStatBar;
    public String type;
    Attribute attribute;
    double defaultValue;
    EquipmentSlot slot;

    public MiningLevelStatDisplay(String type, StatListWidget.TextGetter title, StatListWidget.TextGetter hover) {
        super(0, 0, 76, 19, Component.empty());
        this.type = type;
        text = title;
        this.hover = hover;
        textWidget = new ScrollingTextWidget(getX(), getY(), 80, Component.empty(), FastColor.ARGB32.color(255, 255, 255, 255));
        currentValue = new ScrollingTextWidget(getX(), getY(), 50, Component.empty(), FastColor.ARGB32.color(255, 255, 255, 255));
        centerValue = new ScrollingTextWidget(getX(), getY(), 80 - 10, Component.empty(), FastColor.ARGB32.color(255, 255, 255, 255));
        centerValue.setOrientation(ScrollingTextWidget.Orientation.CENTERED);
        compareValue = new ScrollingTextWidget(getX(), getY(), 80 - 10, Component.empty(), FastColor.ARGB32.color(255, 255, 255, 255));
        compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
        statBar = new StatBar(0, 0, width, 10, FastColor.ARGB32.color(255, 0, 0, 0));
        modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
        hoverDescription = new HoverDescription(getX(), getY(), List.of());
        integerStatBar = new IntegerStatBar(0, 0, width, 1);
        integerStatBar.setGapWidth(2);
        integerStatBar.setHeight(1);
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

    public int getHeightDesired() {
        return 19;
    }

    public int getWidthDesired() {
        int textWidth = Minecraft.getInstance().font.width(this.text.resolve(original).getString());
        int numberWidth = Minecraft.getInstance().font.width(compareValue.getText().getString());
        int size = 1;
        if (textWidth + numberWidth > 76-6) {
            size = 2;
        }
        return 76 * size;
    }

    public int getRed(){
        return MiapiConfig.INSTANCE.client.guiColors.red.argb();
    }

    public int getGreen(){
        return MiapiConfig.INSTANCE.client.guiColors.green.argb();
    }

    @Override
    public InteractAbleWidget getHoverWidget() {
        return null ;
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        double oldValue = getValue(original);
        double compareToValue = getValue(compareTo);

        double min = Math.min(minValue, Math.min(oldValue, compareToValue));
        double max = Math.max(maxValue, Math.max(oldValue, compareToValue));

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        drawTextureWithEdge(drawContext, texture, getX(), getY(), 339, 6, 51, 19, width, height, 512, 512, 2);

        textWidget.setX(this.getX() + 3);
        textWidget.setY(this.getY() + 3);
        textWidget.setWidth(this.width - 8);

        int oldMining = getAltValue(original);
        int compareMining = getAltValue(compareTo);
        statBar.setX(this.getX() + 2);
        statBar.setY(this.getY() + 12);
        statBar.setWidth(this.width - 4);
        statBar.setHeight(1);
        integerStatBar.setX(this.getX() + 2);
        integerStatBar.setY(this.getY() + 15);
        integerStatBar.setWidth(this.width - 4);
        integerStatBar.setHeight(1);
        if (oldMining < compareMining) {
            integerStatBar.setPrimary(oldMining, FastColor.ARGB32.color(255, 255, 255, 255));
            integerStatBar.setSecondary(compareMining, getGreen());
        } else {
            integerStatBar.setPrimary(compareMining, FastColor.ARGB32.color(255, 255, 255, 255));
            integerStatBar.setSecondary(oldMining,getRed());
        }
        if (oldValue < compareToValue) {
            statBar.setPrimary((oldValue - min) / (max - min), FastColor.ARGB32.color(255, 255, 255, 255));
            statBar.setSecondary((compareToValue - min) / (max - min), getGreen());
            compareValue.textColor = getGreen();
        } else {
            statBar.setPrimary((compareToValue - min) / (max - min), FastColor.ARGB32.color(255, 255, 255, 255));
            statBar.setSecondary((oldValue - min) / (max - min), getRed());
            compareValue.textColor = getRed();
        }
        if (oldValue == compareToValue) {
            currentValue.setX(this.getX() - 3);
            currentValue.setY(this.getY() + 3);
            currentValue.setWidth(this.getWidth());
            currentValue.setText(Component.literal(modifierFormat.format(oldValue)));
            currentValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            compareValue.textColor = getRed();
            currentValue.render(drawContext, mouseX, mouseY, delta);
        } else {
            compareValue.setX(this.getX() - 3);
            compareValue.setY(this.getY() + 3);
            compareValue.setWidth(this.getWidth());
            compareValue.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            compareValue.setText(Component.nullToEmpty(modifierFormat.format(compareToValue)));
            compareValue.render(drawContext, mouseX, mouseY, delta);
        }
        statBar.render(drawContext, mouseX, mouseY, delta);
        integerStatBar.render(drawContext, mouseX, mouseY, delta);
        textWidget.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void renderHover(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            Component text1 = this.hover.resolve(compareTo);
            if(!text1.getString().isEmpty()){
                drawContext.renderTooltip(Minecraft.getInstance().font, this.hover.resolve(compareTo), mouseX, mouseY);
            }
        }
    }

    public static Builder builder(String type) {
        return new Builder(type);
    }

    public static class Builder {
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Component.empty();
        Attribute attribute;
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

        public Builder setAttribute(Attribute attribute) {
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

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public Builder setHoverDescription(Component hoverDescription) {
            this.hoverDescription = (stack) -> hoverDescription;
            return this;
        }

        public Builder setHoverDescription(StatListWidget.TextGetter hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key);
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key + ".description", descriptionArgs);
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
