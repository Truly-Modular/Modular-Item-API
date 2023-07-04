package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class SinglePropertyStatDisplay extends SingleStatDisplayDouble {
    protected SimpleDoubleProperty property;

    protected SinglePropertyStatDisplay(TextGetter title, TextGetter hover, SimpleDoubleProperty property) {
        super(0, 0, 80, 32, title, hover);
        this.property = property;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (property.hasValue(original)) {
            return true;
        }
        if (property.hasValue(compareTo)) {
            return true;
        }
        return false;
    }

    @Override
    public double getValue(ItemStack stack) {
        return property.getValueSafeRaw(stack);
    }

    public static Builder Builder(SimpleDoubleProperty property) {
        return new Builder(property);
    }

    public static class Builder {
        SimpleDoubleProperty property;
        public TextGetter name;
        public TextGetter hoverDescription = (stack) -> Text.empty();
        public String translationKey = "";
        public Object[] descriptionArgs = new Object[]{};
        public DecimalFormat modifierFormat;
        public double min = 0;
        public double max = 100;

        private Builder(SimpleDoubleProperty property) {
            this.property = property;
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setMax(double maxValue){
            max = maxValue;
            return this;
        }

        public Builder setMin(double minValue){
            min = minValue;
            return this;
        }

        public Builder setName(Text name) {
            this.name = (stack) -> name;
            return this;
        }

        public Builder setName(TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setTranslationKey(String key) {
            translationKey = key;
            name = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key, modifierFormat.format(property.getValueSafe(stack)));
            hoverDescription = (stack) -> Text.translatable(Miapi.MOD_ID + ".stat." + key + ".description", modifierFormat.format(property.getValueSafe(stack)));
            return this;
        }

        public Builder setHoverDescription(Text hoverDescription) {
            this.hoverDescription = (stack) -> hoverDescription;
            return this;
        }

        public Builder setHoverDescription(TextGetter hoverDescription) {
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
            return display;
        }
    }
}