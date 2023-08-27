/*
 * MIT License
 *
 * Copyright (c) 2021 OroArmor (Eli Orona)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package smartin.miapi.config.oro_config.screen;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.config.oro_config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage and registration for the config entries to set config items
 */
public class ConfigScreenBuilders {
    private static final Map<Class<? extends ConfigItem<?>>, EntryBuilder<?>> COMMANDS = new HashMap<>();

    static {
        register(BooleanConfigItem.class, (EntryBuilder<Boolean>) (configItem, entryBuilder, config) -> entryBuilder.startBooleanToggle(Text.translatable(configItem.getDetails()), configItem.getValue()).setSaveConsumer(configItem::setValue).setDefaultValue(configItem::getDefaultValue).build());
        register(DoubleConfigItem.class, (EntryBuilder<Double>) (configItem, entryBuilder, config) -> {
            DoubleConfigItem doubleConfigItem = (DoubleConfigItem) configItem;
            return entryBuilder.startDoubleField(Text.translatable(doubleConfigItem.getDetails()), doubleConfigItem.getValue()).setSaveConsumer(doubleConfigItem::setValue).setDefaultValue(doubleConfigItem::getDefaultValue).setMin(doubleConfigItem.getMin()).setMax(doubleConfigItem.getMax()).build();
        });
        register(IntegerConfigItem.class, (EntryBuilder<Integer>) (configItem, entryBuilder, config) -> {
            IntegerConfigItem integerConfigItem = (IntegerConfigItem) configItem;
            return entryBuilder.startIntField(Text.translatable(integerConfigItem.getDetails()), integerConfigItem.getValue()).setSaveConsumer(integerConfigItem::setValue).setDefaultValue(integerConfigItem::getDefaultValue).setMin(integerConfigItem.getMin()).setMax(integerConfigItem.getMax()).build();
        });
        register(StringConfigItem.class, (EntryBuilder<String>) (configItem, entryBuilder, config) -> entryBuilder.startStrField(Text.translatable(configItem.getDetails()), configItem.getValue()).setSaveConsumer(configItem::setValue).setDefaultValue(configItem::getDefaultValue).build());
        register(EnumConfigItem.class, new EnumEntryBuilder<>());
        register(ArrayConfigItem.class, new ArrayEntryBuilder<>());
    }

    /**
     * Registers a new entry builder for the config item
     *
     * @param configItemClass The class for the config item
     * @param builder         The entry builder for the item
     * @param <T>             The config item type
     */
    public static <T extends ConfigItem<?>> void register(Class<T> configItemClass, EntryBuilder<?> builder) {
        if (COMMANDS.containsKey(configItemClass)) {
            throw new IllegalArgumentException("Duplicate entries for " + configItemClass.getSimpleName());
        }

        COMMANDS.put(configItemClass, builder);
    }

    /**
     * Gets the entry builder for the config item
     *
     * @param configItem The config item
     * @param <T>        The storage type for the config item
     * @param <C>        The type of the config item
     * @return The entry builder
     */
    @SuppressWarnings("unchecked")
    public static <T, C extends ConfigItem<T>> EntryBuilder<T> getEntryBuilder(C configItem) {
        return (EntryBuilder<T>) COMMANDS.getOrDefault(configItem.getClass(), (EntryBuilder<T>) (configItem1, entryBuilder, config) -> {
            throw new IllegalArgumentException("Unknown Config Type");
        });
    }

    /**
     * An interface to create new entries for the config screen
     *
     * @param <T> The storage type of the config item
     */
    public interface EntryBuilder<T> {
        /**
         * Gets the entry for the config item
         *
         * @param configItem   The config item
         * @param entryBuilder The cloth config entry builder
         * @param config       The config
         * @return A new entry for the config screen
         */
        AbstractConfigListEntry<?> getConfigEntry(ConfigItem<T> configItem, ConfigEntryBuilder entryBuilder, Config config);
    }

    @SuppressWarnings("all")
    private static class ArrayEntryBuilder<T> implements EntryBuilder<T[]> {
        @Override
        public AbstractConfigListEntry<?> getConfigEntry(ConfigItem<T[]> configItem, ConfigEntryBuilder entryBuilder, Config config) {
            ArrayConfigItem<T> arrayConfigItem = (ArrayConfigItem<T>) configItem;
            switch (arrayConfigItem.getDefaultValue()[0].getClass().isEnum() ? "ENUM" : arrayConfigItem.getDefaultValue()[0].getClass().getSimpleName().toUpperCase()) {
                case "BOOLEAN":
                    List<AbstractConfigListEntry> bconfigs = new ArrayList<>();
                    for (int i = 0; i < arrayConfigItem.getValue().length; i++) {
                        int finalI = i;
                        AbstractConfigListEntry<?> entry = entryBuilder.startBooleanToggle(Text.translatable(arrayConfigItem.getDetails()).append(": " + i), (Boolean) arrayConfigItem.getValue(i)).setSaveConsumer(val -> arrayConfigItem.setValue((T) val, finalI)).setDefaultValue(() -> (Boolean) arrayConfigItem.getDefaultValue(finalI)).build();
                        bconfigs.add(entry);
                    }
                    return entryBuilder.startSubCategory(Text.translatable(configItem.getDetails()), bconfigs).build();

                case "INTEGER":
                    List<AbstractConfigListEntry> iconfigs = new ArrayList<>();
                    for (int i = 0; i < arrayConfigItem.getValue().length; i++) {
                        int finalI = i;
                        AbstractConfigListEntry<?> entry = entryBuilder.startIntField(Text.translatable(arrayConfigItem.getDetails()).append(": " + i), (Integer) arrayConfigItem.getValue(i)).setSaveConsumer(val -> arrayConfigItem.setValue((T) val, finalI)).setDefaultValue(() -> (Integer) arrayConfigItem.getDefaultValue(finalI)).build();
                        iconfigs.add(entry);
                    }
                    return entryBuilder.startSubCategory(Text.translatable(configItem.getDetails()), iconfigs).build();
                case "DOUBLE":
                    List<AbstractConfigListEntry> dconfigs = new ArrayList<>();
                    for (int i = 0; i < arrayConfigItem.getValue().length; i++) {
                        int finalI = i;
                        AbstractConfigListEntry<?> entry = entryBuilder.startDoubleField(Text.translatable(arrayConfigItem.getDetails()).append(": " + i), (Double) arrayConfigItem.getValue(i)).setSaveConsumer(val -> arrayConfigItem.setValue((T) val, finalI)).setDefaultValue(() -> (Double) arrayConfigItem.getDefaultValue(finalI)).build();
                        dconfigs.add(entry);
                    }
                    return entryBuilder.startSubCategory(Text.translatable(configItem.getDetails()), dconfigs).build();

                case "STRING":
                    List<AbstractConfigListEntry> sconfigs = new ArrayList<>();
                    for (int i = 0; i < arrayConfigItem.getValue().length; i++) {
                        int finalI = i;
                        AbstractConfigListEntry<?> entry = entryBuilder.startStrField(Text.translatable(arrayConfigItem.getDetails()).append(": " + i), (String) arrayConfigItem.getValue(i)).setSaveConsumer(val -> arrayConfigItem.setValue((T) val, finalI)).setDefaultValue(() -> (String) arrayConfigItem.getDefaultValue(finalI)).build();
                        sconfigs.add(entry);
                    }
                    return entryBuilder.startSubCategory(Text.translatable(arrayConfigItem.getDetails()), sconfigs).build();

                case "ENUM":
                    return getEnumArrayEntry(entryBuilder, (ArrayConfigItem) arrayConfigItem);

                default:
                    throw new IllegalStateException("Class " + arrayConfigItem.getDefaultValue()[0].getClass().getSimpleName() + " is an unsupported type");
            }
        }

        @NotNull
        private <S extends Enum<S>> AbstractConfigListEntry<?> getEnumArrayEntry(ConfigEntryBuilder entryBuilder, ArrayConfigItem<S> arrayConfigItem) {
            List<AbstractConfigListEntry> configs = new ArrayList<>();
            for (int i = 0; i < arrayConfigItem.getValue().length; i++) {
                int finalI = i;
                AbstractConfigListEntry<?> entry = entryBuilder.startEnumSelector(Text.translatable(arrayConfigItem.getDetails()).append(": " + i), (Class<S>) ((Enum<?>) arrayConfigItem.getValue(i)).getClass().getEnumConstants()[0].getClass(), arrayConfigItem.getValue(i))
                        .setSaveConsumer(val -> arrayConfigItem.setValue((S) val, finalI))
                        .setDefaultValue(() -> (S) arrayConfigItem.getDefaultValue(finalI)).build();
                configs.add(entry);
            }

            return entryBuilder.startSubCategory(Text.translatable(arrayConfigItem.getDetails()), configs).build();
        }
    }

    private static class EnumEntryBuilder<T extends Enum<?>> implements EntryBuilder<T> {
        @SuppressWarnings("unchecked")
        @Override
        public AbstractConfigListEntry<?> getConfigEntry(ConfigItem<T> configItem, ConfigEntryBuilder entryBuilder, Config config) {
            return entryBuilder.startEnumSelector(Text.translatable(configItem.getDetails()), (Class<T>) ((Enum<?>) configItem.getValue()).getClass().getEnumConstants()[0].getClass(), configItem.getValue())
                    .setSaveConsumer(configItem::setValue)
                    .setDefaultValue(configItem::getValue).build();
        }
    }
}
