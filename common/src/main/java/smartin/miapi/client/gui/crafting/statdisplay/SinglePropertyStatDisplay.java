package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.BiFunction;

@Environment(EnvType.CLIENT)
public class SinglePropertyStatDisplay extends SingleStatDisplayDouble {
    protected DoubleProperty property;
    public BiFunction<ItemStack, ItemStack, Boolean> condition = (old, compare) -> true;

    protected SinglePropertyStatDisplay(StatListWidget.TextGetter title, StatListWidget.TextGetter hover, DoubleProperty property) {
        super(0, 0, 51, 19, title, hover);
        this.property = property;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (!condition.apply(original, compareTo)) {
            return false;
        }
        return property.getValue(original).isPresent() || property.getValue(compareTo).isPresent();
    }

    @Override
    public double getValue(ItemStack stack) {
        return property.getValue(stack).orElse(0.0);
    }

    public DoubleOperationResolvable getResolvable(ItemStack stack) {
        return property.getData(stack).orElse(null);
    }

    public static String stringForOperation(DecimalFormat format, DoubleOperationResolvable.IndividualOperation resolvable) {
        String number = "" + format.format(resolvable.solve());
        String operation = getStringName(resolvable.attributeOperation);
        if (operation.equals("+") && number.startsWith("-")) {
            return number + " " + resolvable.instance.getModuleName().getString();
        }
        return operation + number + " " + resolvable.instance.getModuleName().getString();
    }

    public static String getStringName(DoubleOperationResolvable.IndividualOperation.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> "+";
            case ADD_MULTIPLIED_BASE -> "*";
            case ADD_MULTIPLIED_TOTAL -> "**";
            case CUSTOM_TOTAL -> "custom ";
        };
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
        public DecimalFormat hoverFormat;
        public double min = 0;
        public double max = 100;
        public boolean inverse = false;
        public BiFunction<ItemStack, ItemStack, Boolean> condition = (old, compare) -> true;
        public Component postFix = Component.empty();

        private Builder(DoubleProperty property) {
            this.property = property;
            modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
                decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
        }

        public Builder setInverse(boolean inverse) {
            this.inverse = inverse;
            return this;
        }

        public Builder setCondition(BiFunction<ItemStack, ItemStack, Boolean> condition) {
            this.condition = condition;
            return this;
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

        public Builder setTranslationKey(ResourceLocation key) {
            translationKey = Miapi.toLangString(key);
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + translationKey, modifierFormat.format(property.getValue(stack).orElse(0.0)));
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + translationKey + ".description", modifierFormat.format(property.getValue(stack).orElse(0.0)));
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

        public Builder setPostFix(Component format) {
            this.postFix = format;
            return this;
        }

        public Builder setHoverFormat(String format) {
            hoverFormat = Util.make(new DecimalFormat(format), (decimalFormat) -> {
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
            display.inverse = inverse;
            display.condition = condition;
            display.postfix = postFix;
            if (hoverFormat == null) {
                hoverFormat = modifierFormat;
            }
            display.hoverFormat = hoverFormat;
            return display;
        }
    }
}