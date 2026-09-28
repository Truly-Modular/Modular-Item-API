package smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.statdisplay.rework.StatTypes;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.*;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Function;

public record DoubleStatData(
        Function<ItemStack, DoubleOperationResolvable> getter,
        double min,
        double max,
        double defaultValue,
        boolean inverse,
        double priority
) implements StatMetaData<DoubleOperationResolvable> {

    @Override
    public DoubleOperationResolvable getData(ItemStack stack) {
        return getter.apply(stack);
    }

    @Override
    public double getPriority() {
        return priority;
    }

    public static DoubleStatBuilder forProperty(
            ResourceLocation id,
            StatGroup group,
            DoubleProperty property
    ) {
        return new DoubleStatBuilder(id, group, property);
    }

    public static DoubleStatBuilder getBuilder(
            ResourceLocation id,
            StatText<DoubleOperationResolvable, DoubleStatData> name,
            StatText<DoubleOperationResolvable, DoubleStatData> description,
            StatGroup group,
            Function<ItemStack, DoubleOperationResolvable> getter
    ) {
        return new DoubleStatBuilder(id, name, description, group, getter);
    }

    public record NumberWrapper(Number value, DecimalFormat formatter) {
        public String format() {
            return formatter.format(value);
        }

        public double doubleValue() {
            return value.doubleValue();
        }

        public int intValue() {
            return value.intValue();
        }
    }

    public static class DoubleStatBuilder {

        private final ResourceLocation id;
        private StatText<DoubleOperationResolvable, DoubleStatData> name;
        private StatText<DoubleOperationResolvable, DoubleStatData> description;
        private final StatGroup group;
        private final Function<ItemStack, DoubleOperationResolvable> getter;

        private double min = 0;
        private double max = 100;
        private double defaultValue = 0;
        private double priority = 0;
        private DecimalFormat formatter = createFormatter("##.##");
        private boolean inverse;

        private DoubleStatBuilder(
                ResourceLocation id,
                StatGroup group,
                DoubleProperty property
        ) {
            this.id = id;

            String translationKey = Miapi.toLangString(id);
            this.name = (stack, data, meta) ->
                    Component.translatable(
                            Miapi.MOD_ID + ".stat." + translationKey,
                            new NumberWrapper(
                                    property.getValue(stack).orElse(meta.defaultValue),
                                    formatter
                            )
                    );

            this.description = (stack, data, meta) ->
                    Component.translatable(
                            Miapi.MOD_ID + ".stat." + translationKey + ".description",
                            new NumberWrapper(
                                    property.getValue(stack).orElse(meta.defaultValue),
                                    formatter
                            )
                    );

            this.group = group;
            this.getter = stack -> property.getData(stack).orElse(null);
            this.defaultValue = property.baseValue;
        }

        private DoubleStatBuilder(
                ResourceLocation id,
                StatText<DoubleOperationResolvable, DoubleStatData> name,
                StatText<DoubleOperationResolvable, DoubleStatData> description,
                StatGroup group,
                Function<ItemStack, DoubleOperationResolvable> getter
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.group = group;
            this.getter = getter;
        }

        /**
         * Sets the minimum value displayed by this stat.
         */
        public DoubleStatBuilder setName(StatText<DoubleOperationResolvable, DoubleStatData> name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the minimum value displayed by this stat.
         */
        public DoubleStatBuilder setDescription(StatText<DoubleOperationResolvable, DoubleStatData> description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the minimum value displayed by this stat.
         */
        public DoubleStatBuilder setMin(double min) {
            this.min = min;
            return this;
        }

        /**
         * Sets the maximum value displayed by this stat.
         */
        public DoubleStatBuilder setMax(double max) {
            this.max = max;
            return this;
        }

        /**
         * Sets the value used when no value is available.
         */
        public DoubleStatBuilder setDefaultValue(double defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets the priority used when rendering this stat. Lower values are rendered earlier.
         */
        public DoubleStatBuilder setPriority(double priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Sets the decimal format used by the default {@link NumberWrapper}.
         */
        public DoubleStatBuilder setFormat(String format) {
            this.formatter = createFormatter(format);
            return this;
        }

        /**
         * Sets the formatter used by the default {@link NumberWrapper}.
         */
        public DoubleStatBuilder setFormatter(DecimalFormat formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * Inverts the stat's normalized display value.
         */
        public DoubleStatBuilder setInverse(boolean inverse) {
            this.inverse = inverse;
            return this;
        }

        /**
         * Builds the stat definition.
         */
        public Stat<DoubleOperationResolvable, DoubleStatData> build() {
            var metaData = new DoubleStatData(
                    getter,
                    min,
                    max,
                    defaultValue,
                    inverse,
                    priority
            );

            return new Stat<>(
                    id.withPrefix("double." + group.getID().toLanguageKey() + "."),
                    name,
                    description,
                    StatTypes.DOUBLE_RESOLVEABLE_TYPE,
                    group,
                    metaData
            );
        }

        private static DecimalFormat createFormatter(String format) {
            var formatter = new DecimalFormat(format);
            formatter.setDecimalFormatSymbols(
                    DecimalFormatSymbols.getInstance(Locale.ROOT)
            );
            return formatter;
        }
    }
}