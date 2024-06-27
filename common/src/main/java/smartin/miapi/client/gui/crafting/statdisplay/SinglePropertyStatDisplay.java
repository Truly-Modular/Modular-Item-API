package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class SinglePropertyStatDisplay extends SingleStatDisplayDouble {
    protected DoubleProperty property;

    protected SinglePropertyStatDisplay(StatListWidget.TextGetter title, StatListWidget.TextGetter hover, DoubleProperty property) {
        super(0, 0, 51, 19, title, hover);
        this.property = property;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        return (property.hasValue(original) && property.getValue(original) != 0) || (property.hasValue(compareTo) && property.getValue(compareTo) != 0);
    }

    @Override
    public double getValue(ItemStack stack) {
        return property.getValueSafeRaw(stack);
    }

    public static Builder builder(DoubleProperty property) {
        return new Builder(property);
    }

    public static class Builder {
        DoubleProperty property;
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Component.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 100;

        private Builder(DoubleProperty property) {
            this.property = property;
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setMax(double maxValue) {
            max = maxValue;
            return this;
        }

        public Builder setMin(double minValue) {
            min = minValue;
            return this;
        }

        public Builder setName(Component name) {
            this.name = (stack) -> name;
            return this;
        }

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key, modifierFormat.format(property.getValueSafe(stack)));
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + key + ".description", modifierFormat.format(property.getValueSafe(stack)));
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

        public Builder setFormat(String format) {
            modifierFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public SinglePropertyStatDisplay build() {
            // Validate the required fields
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (property == null) {
                throw new IllegalStateException("Property is required");
            }

            // Create an instance of AttributeProperty with the builder values
            SinglePropertyStatDisplay display = new SinglePropertyStatDisplay(name, hoverDescription, property);
            display.maxValue = max;
            display.minValue = min;
            display.modifierFormat = modifierFormat;
            return display;
        }
    }
}