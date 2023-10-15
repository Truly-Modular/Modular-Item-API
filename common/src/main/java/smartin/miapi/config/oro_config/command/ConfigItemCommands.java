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

package smartin.miapi.config.oro_config.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import smartin.miapi.config.oro_config.*;

import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * Storage and registration for the commands to set config items
 */
public final class ConfigItemCommands {
    private static final Map<Class<? extends ConfigItem<?>>, CommandBuilder<?>> COMMANDS = new HashMap<>();

    static {
        register(BooleanConfigItem.class, new CommandBuilder<Boolean>() {
            @Override
            public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<Boolean> configItem, ConfigItemGroup group, Config config) {
                return RequiredArgumentBuilder.<S, Boolean>argument("boolean", BoolArgumentType.bool()).executes(c -> {
                    boolean result = BoolArgumentType.getBool(c, "boolean");
                    configItem.setValue(result);
                    config.saveConfigToFile();
                    return 1;
                });
            }
        });
        register(DoubleConfigItem.class, new CommandBuilder<Double>() {
            @Override
            public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<Double> configItem, ConfigItemGroup group, Config config) {
                DoubleConfigItem doubleConfigItem = (DoubleConfigItem) configItem;
                return RequiredArgumentBuilder.<S, Double>argument("double", DoubleArgumentType.doubleArg(doubleConfigItem.getMin(), doubleConfigItem.getMax())).executes(c -> {
                    double result = DoubleArgumentType.getDouble(c, "double");
                    doubleConfigItem.setValue(result);
                    config.saveConfigToFile();
                    return 1;
                });
            }
        });
        register(IntegerConfigItem.class, new CommandBuilder<Integer>() {
            @Override
            public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<Integer> configItem, ConfigItemGroup group, Config config) {
                IntegerConfigItem integerConfigItem = (IntegerConfigItem) configItem;
                return RequiredArgumentBuilder.<S, Integer>argument("int", IntegerArgumentType.integer(integerConfigItem.getMin(), integerConfigItem.getMax())).executes(c -> {
                    int result = IntegerArgumentType.getInteger(c, "int");
                    integerConfigItem.setValue(result);
                    config.saveConfigToFile();
                    return 1;
                });
            }
        });
        register(EnumConfigItem.class, new EnumCommandBuilder<>());
        register(StringConfigItem.class, new CommandBuilder<String>() {
            @Override
            public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<String> configItem, ConfigItemGroup group, Config config) {
                return RequiredArgumentBuilder.<S, String>argument("string", StringArgumentType.string()).executes(c -> {
                    String result = StringArgumentType.getString(c, "string");
                    configItem.setValue(result);
                    config.saveConfigToFile();
                    return 1;
                });
            }
        });
        register(ArrayConfigItem.class, new ArrayCommandBuilder<>());
    }

    /**
     * Registers a new command builder for the config item
     *
     * @param configItemClass The class for the config item
     * @param builder         The builder to use
     * @param <T>             A config item type
     */
    public static <T extends ConfigItem<?>> void register(Class<T> configItemClass, CommandBuilder<?> builder) {
        if (COMMANDS.containsKey(configItemClass)) {
            throw new IllegalArgumentException("Duplicate entries for " + configItemClass.getSimpleName());
        }

        COMMANDS.put(configItemClass, builder);
    }

    /**
     * Gets the config builder for the config item
     *
     * @param configItem The config item to getRaw the builder for
     * @param <T>        The type of the config item's value
     * @param <C>        The type for the config item
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T, C extends ConfigItem<T>> CommandBuilder<T> getCommandBuilder(C configItem) {
        return (CommandBuilder<T>) COMMANDS.getOrDefault(configItem.getClass(), new CommandBuilder<T>() {
            @Override
            public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<T> configItem, ConfigItemGroup group, Config config) {
                throw new IllegalArgumentException("Unknown Config Type");
            }
        });
    }

    /**
     * An interface to build new commands
     *
     * @param <T> The type for the config item the command builder is for
     */
    public interface CommandBuilder<T> {
        /**
         * Gets the command for the config item
         *
         * @param configItem The config item
         * @param group      The group for the item
         * @param config     The config for the item
         * @param <S>        The command source type
         * @return A command for the config item
         */
        <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<T> configItem, ConfigItemGroup group, Config config);
    }

    private static class ArrayCommandBuilder<T> implements CommandBuilder<T[]> {
        @SuppressWarnings("unchecked")
        @Override
        public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<T[]> configItem, ConfigItemGroup group, Config config) {
            ArgumentBuilder<S, ?> setCommand;
            ArrayConfigItem<T> arrayConfigItem = (ArrayConfigItem<T>) configItem;
            switch (arrayConfigItem.getDefaultValue()[0].getClass().isEnum() ? "ENUM" : arrayConfigItem.getDefaultValue()[0].getClass().getSimpleName().toUpperCase()) {
                case "BOOLEAN":
                    setCommand = RequiredArgumentBuilder.<S, Boolean>argument("boolean", BoolArgumentType.bool()).executes(c -> {
                        boolean result = BoolArgumentType.getBool(c, "boolean");
                        int index = IntegerArgumentType.getInteger(c, "index");
                        arrayConfigItem.setValue((T) (Object) result, index);
                        config.saveConfigToFile();
                        return 1;
                    });
                    break;

                case "INTEGER":
                    setCommand = RequiredArgumentBuilder.<S, Integer>argument("int", IntegerArgumentType.integer()).executes(c -> {
                        int result = IntegerArgumentType.getInteger(c, "int");
                        int index = IntegerArgumentType.getInteger(c, "index");
                        arrayConfigItem.setValue((T) (Object) result, index);
                        config.saveConfigToFile();
                        return 1;
                    });
                    break;
                case "DOUBLE":
                    setCommand = RequiredArgumentBuilder.<S, Double>argument("double", DoubleArgumentType.doubleArg()).executes(c -> {
                        double result = DoubleArgumentType.getDouble(c, "double");
                        int index = IntegerArgumentType.getInteger(c, "index");
                        arrayConfigItem.setValue((T) (Object) result, index);
                        config.saveConfigToFile();
                        return 1;
                    });
                    break;

                case "STRING":
                    setCommand = RequiredArgumentBuilder.<S, String>argument("string", StringArgumentType.greedyString()).executes(c -> {
                        String result = StringArgumentType.getString(c, "string");
                        int index = IntegerArgumentType.getInteger(c, "index");
                        arrayConfigItem.setValue((T) result, index);
                        config.saveConfigToFile();
                        return 1;
                    });
                    break;

                case "ENUM":
                    setCommand = literal("set");
                    Enum<?>[] enums = ((Enum<?>) arrayConfigItem.getValue()[0]).getClass().getEnumConstants();
                    for (Enum<?> _enum : enums) {
                        setCommand.then(LiteralArgumentBuilder.<S>literal(_enum.toString()).executes(c -> {
                            int index = IntegerArgumentType.getInteger(c, "index");
                            arrayConfigItem.setValue((T) _enum, index);
                            config.saveConfigToFile();
                            return 1;
                        }));
                    }
                    break;

                default:
                    throw new IllegalStateException("Class " + arrayConfigItem.getDefaultValue()[0].getClass().getSimpleName() + " is an unsupported type");
            }
            return RequiredArgumentBuilder.<S, Integer>argument("index", IntegerArgumentType.integer(0, arrayConfigItem.getValue().length)).then(setCommand);
        }
    }

    private static class EnumCommandBuilder<T extends Enum<?>> implements CommandBuilder<T> {
        @SuppressWarnings("unchecked")
        @Override
        public <S extends CommandSource> ArgumentBuilder<S, ?> getCommand(ConfigItem<T> configItem, ConfigItemGroup group, Config config) {
            LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal("set");
            Enum<?>[] enums = ((Enum<?>) configItem.getValue()).getClass().getEnumConstants();
            for (Enum<?> _enum : enums) {
                builder.then(LiteralArgumentBuilder.<S>literal(_enum.toString()).executes(c -> {
                    configItem.setValue((T) _enum);
                    config.saveConfigToFile();
                    return 1;
                }));
            }
            return builder;
        }
    }
}
