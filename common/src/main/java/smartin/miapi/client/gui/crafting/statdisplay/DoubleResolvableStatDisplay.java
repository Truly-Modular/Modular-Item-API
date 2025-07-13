package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class DoubleResolvableStatDisplay extends SingleStatDisplayDouble {
    protected Function<ItemStack, Optional<DoubleOperationResolvable>> resolvableGetter;
    public BiFunction<ItemStack, ItemStack, Boolean> condition = (old, compare) -> true;

    protected DoubleResolvableStatDisplay(
            StatListWidget.TextGetter title,
            StatListWidget.TextGetter hover,
            Function<ItemStack, Optional<DoubleOperationResolvable>> resolvableGetter
    ) {
        super(0, 0, 51, 19, title, hover);
        this.resolvableGetter = resolvableGetter;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        if (!condition.apply(original, compareTo)) {
            return false;
        }
        return resolvableGetter.apply(original).isPresent() || resolvableGetter.apply(compareTo).isPresent();
    }

    @Override
    public double getValue(ItemStack stack) {
        return resolvableGetter.apply(stack).map(DoubleOperationResolvable::getValue).orElse(0.0);
    }

    public DoubleOperationResolvable getResolvable(ItemStack stack) {
        return resolvableGetter.apply(stack).orElse(null);
    }

    public static String getStringName(AttributeModifier.Operation operation) {
        return switch (operation) {
            case AttributeModifier.Operation.ADD_VALUE -> "+";
            case ADD_MULTIPLIED_BASE -> "*";
            case ADD_MULTIPLIED_TOTAL -> "**";
        };
    }

    public static Builder builder(Function<ItemStack, Optional<DoubleOperationResolvable>> getter) {
        return new Builder(getter);
    }

    public static class Builder {
        Function<ItemStack, Optional<DoubleOperationResolvable>> getter;
        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = (stack) -> Component.empty();
        public DecimalFormat modifierFormat;
        public DecimalFormat hoverFormat;
        public double min = 0;
        public double max = 100;
        public boolean inverse = false;
        public BiFunction<ItemStack, ItemStack, Boolean> condition = (old, compare) -> {
            return getter.apply(old).isPresent() && !getter.apply(old).get().operations.isEmpty() ||
                   getter.apply(compare).isPresent() && !getter.apply(compare).get().operations.isEmpty();
        };

        private Builder(Function<ItemStack, Optional<DoubleOperationResolvable>> getter) {
            this.getter = getter;
            modifierFormat = Util.make(new DecimalFormat("##.##"), df -> {
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
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
            this.max = maxValue;
            return this;
        }

        public Builder setMin(double minValue) {
            this.min = minValue;
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
            String translationKey = Miapi.toLangString(key);
            name = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + translationKey,
                    modifierFormat.format(getter.apply(stack).map(DoubleOperationResolvable::getValue).orElse(0.0)));
            hoverDescription = (stack) -> Component.translatable(Miapi.MOD_ID + ".stat." + translationKey + ".description",
                    modifierFormat.format(getter.apply(stack).map(DoubleOperationResolvable::getValue).orElse(0.0)));
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
            modifierFormat = Util.make(new DecimalFormat(format), df -> {
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public Builder setHoverFormat(String format) {
            hoverFormat = Util.make(new DecimalFormat(format), df -> {
                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
            });
            return this;
        }

        public DoubleResolvableStatDisplay build() {
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }
            if (getter == null) {
                throw new IllegalStateException("Resolvable getter is required");
            }

            DoubleResolvableStatDisplay display = new DoubleResolvableStatDisplay(name, hoverDescription, getter);
            display.maxValue = max;
            display.minValue = min;
            display.modifierFormat = modifierFormat;
            display.inverse = inverse;
            display.condition = condition;
            display.hoverFormat = hoverFormat != null ? hoverFormat : modifierFormat;
            return display;
        }
    }
}
