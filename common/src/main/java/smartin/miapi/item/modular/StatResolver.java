package smartin.miapi.item.modular;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.misc.CustomIntermediateCodec;
import com.redpxnda.nucleus.codec.misc.IntermediateCodec;
import net.minecraft.text.Text;
import org.mariuszgromada.math.mxparser.Expression;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for resolving dynamic values in item stats and descriptions in relation to a {@link ItemModule.ModuleInstance}.
 */
public class StatResolver {
    public static final class Codecs {
        public static Codec<String> STRING(ItemModule.ModuleInstance instance) {
            return Codec.STRING.xmap(s -> resolveString(s, instance), s -> s);
        }

        public static Codec<Double> DOUBLE(ItemModule.ModuleInstance instance) {
            return Codec.either(Codec.DOUBLE, Codec.STRING.xmap(s -> resolveDouble(s, instance), String::valueOf)).xmap(e -> {
                if (e.left().isPresent()) return e.left().get();
                else return e.right().get();
            }, Either::left);
        }

        public static Codec<Integer> INTEGER(ItemModule.ModuleInstance instance) {
            return Codec.either(Codec.INT, DOUBLE(instance).xmap(Double::intValue, Integer::doubleValue)).xmap(e -> {
                if (e.left().isPresent()) return e.left().get();
                else return e.right().get();
            }, Either::left);
        }
    }

    @AutoCodec.Override("codec")
    public static class StringFromStat extends IntermediateCodec.Median<String, ItemModule.ModuleInstance, String> {
        public static BiFunction<String, ItemModule.ModuleInstance, String> func = StatResolver::resolveString;
        public static Codec<StringFromStat> codec = new CustomIntermediateCodec<>(Codec.STRING, func, (s, b) -> new StringFromStat(s));

        public StringFromStat(String start) {
            super(start, func);
        }
    }

    @AutoCodec.Override("fullCodec")
    public static class DoubleFromStat extends IntermediateCodec.Median<String, ItemModule.ModuleInstance, Double> {
        public static BiFunction<String, ItemModule.ModuleInstance, Double> func = StatResolver::resolveDouble;
        public static Codec<DoubleFromStat> codec = new CustomIntermediateCodec<>(Codec.STRING, func, (s, b) -> new DoubleFromStat(s));
        public static Codec<DoubleFromStat> fullCodec =
                Codec.either(
                        Codec.DOUBLE,
                        codec
                ).xmap(either -> {
                    if (either.right().isPresent())
                        return either.right().get();
                    else
                        return new DoubleFromStat(either.left().get());
                }, Either::right);

        public DoubleFromStat(String start) {
            super(start, func);
        }

        public DoubleFromStat(double start) {
            this(String.valueOf(start));
        }

        @Override
        public String toString() {
            return "DoubleFromStat{" +
                    "start=" + start +
                    '}';
        }
    }

    @AutoCodec.Override("fullCodec")
    public static class IntegerFromStat extends IntermediateCodec.Median<String, ItemModule.ModuleInstance, Integer> {
        public static BiFunction<String, ItemModule.ModuleInstance, Integer> func = (raw, input) -> (int) resolveDouble(raw, input);
        public static Codec<IntegerFromStat> codec = new CustomIntermediateCodec<>(Codec.STRING, func, (s, b) -> new IntegerFromStat(s));
        public static Codec<IntegerFromStat> fullCodec =
                Codec.either(
                        Codec.INT,
                        codec
                ).xmap(either -> {
                    if (either.right().isPresent())
                        return either.right().get();
                    else
                        return new IntegerFromStat(either.left().get());
                }, Either::right);

        public IntegerFromStat(String start) {
            super(start, func);
        }

        public IntegerFromStat(int start) {
            this(String.valueOf(start));
        }

        @Override
        public String toString() {
            return "IntegerFromStat{" +
                    "start=" + start +
                    '}';
        }
    }

    /**
     * A map of resolvers, keyed by resolver keyword.
     */
    private static final Map<String, Resolver> resolverMap = new ConcurrentHashMap<>();

    static {
        StatResolver.registerResolver("translation", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                return Double.parseDouble(Text.translatable(data).getString());
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                return Text.translatable(data).getString();
            }
        });
        StatResolver.registerResolver("collect", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                if (data.contains(".")) {
                    String[] parts = data.split("\\.", 2);
                    Stream<Double> numbers = instance.getRoot().allSubModules().stream().map(module -> StatResolver.resolveDouble(parts[1], module));
                    double result = 0;
                    switch (parts[1]) {
                        case "add":
                            result = numbers.collect(Collectors.summarizingDouble(Double::doubleValue)).getSum();
                            break;
                        case "max":
                            result = numbers.collect(Collectors.summarizingDouble(Double::doubleValue)).getMax();
                            break;
                        case "min":
                            result = numbers.collect(Collectors.summarizingDouble(Double::doubleValue)).getMin();
                            break;
                        case "average":
                            result = numbers.collect(Collectors.averagingDouble(Double::doubleValue));
                            break;
                        default: {
                            Miapi.LOGGER.warn("invalid collect Operation " + parts[1] + " add, max, min, average are valid operations");
                        }
                    }
                    return result;
                }
                return 0;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                return null;
            }
        });
        StatResolver.registerResolver("material-module", new StatResolver.Resolver() {

            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                double firstResult = StatResolver.resolveDouble("material." + data, instance);
                if (firstResult == 0) {
                    return StatResolver.resolveDouble("module." + data, instance);
                }
                return firstResult;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                String firstResult = StatResolver.resolveString("material." + data, instance);
                if (firstResult == null || firstResult.equals("")) {
                    return StatResolver.resolveString("module." + data, instance);
                }
                return firstResult;
            }
        });
        StatResolver.registerResolver("module-material", new StatResolver.Resolver() {

            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                double firstResult = StatResolver.resolveDouble("module." + data, instance);
                if (firstResult == 0) {
                    return StatResolver.resolveDouble("material." + data, instance);
                }
                return firstResult;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                String firstResult = StatResolver.resolveString("module." + data, instance);
                if (firstResult == null || firstResult.equals("")) {
                    return StatResolver.resolveString("material." + data, instance);
                }
                return firstResult;
            }
        });
    }

    /**
     * Resolves all string values contained in square brackets in the input string.
     *
     * @param raw      the input string
     * @param instance the module instance for which to resolve values
     * @return the resolved string
     */
    public static String resolveString(String raw, ItemModule.ModuleInstance instance) {
        String resolved = raw;
        Pattern pattern = Pattern.compile("\\[([^\\[]*?)\\]"); // regex pattern to match text inside square brackets
        Matcher matcher = pattern.matcher(raw);
        int counter = 10;
        while (matcher.find() && counter > 0) {
            counter--;
            String match = matcher.group(1);
            String[] parts = match.split("\\."); // split by dot
            if (parts.length >= 2) {
                String resolverKey = parts[0];
                String resolverData = String.join(".", Arrays.copyOfRange(parts, 1, parts.length)); // join the remaining parts with dots
                Resolver resolver = resolverMap.get(resolverKey);
                if (resolver != null) {
                    String resolvedData = resolver.resolveString(resolverData, instance);
                    resolved = resolved.replace("[" + match + "]", resolvedData);
                } else {
                    resolved = resolved.replace("[" + match + "]", "");
                }
            } else {
                resolved = resolved.replace("[" + match + "]", "");
            }
            matcher = pattern.matcher(resolved);
        }
        return resolved;
    }

    public static double resolveDouble(JsonElement raw, ItemModule.ModuleInstance instance) {
        try {
            return raw.getAsDouble();
        } catch (Exception exception) {
            return resolveDouble(raw.getAsString(), instance);
        }
    }

    /**
     * Resolves all double values contained in square brackets in the input string, and evaluates the resulting expression.
     *
     * @param raw      the input string
     * @param instance the module instance for which to resolve values
     * @return the evaluated result
     */
    public static double resolveDouble(String raw, ItemModule.ModuleInstance instance) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception exception) {

        }
        String resolved = raw;
        Pattern pattern = Pattern.compile("\\[(.*?)\\]"); // regex pattern to match text inside square brackets
        Matcher matcher = pattern.matcher(raw);
        while (matcher.find()) {
            String match = matcher.group(1);
            String[] parts = match.split("\\."); // split by dot
            if (parts.length >= 2) {
                String resolverKey = parts[0];
                String resolverData = String.join(".", Arrays.copyOfRange(parts, 1, parts.length)); // join the remaining parts with dots
                Resolver resolver = resolverMap.get(resolverKey);
                if (resolver != null) {
                    String resolvedData = String.valueOf(resolver.resolveDouble(resolverData, instance));
                    resolved = resolved.replace("[" + match + "]", resolvedData);
                } else {
                    resolved = resolved.replace("[" + match + "]", "");
                }
            }
        }
        Expression e = new Expression(resolved);
        return e.calculate();
    }

    /**
     * Translates and resolves all string values contained in square brackets in the input string.
     *
     * @param raw      the input string
     * @param instance the module instance for which to resolve values
     * @return the translated and resolved text
     */
    public static Text translateAndResolve(String raw, ItemModule.ModuleInstance instance) {
        String old = "";
        String newString = raw;
        for (int i = 0; i < 100 && !old.equals(newString); i++) {
            old = newString;
            newString = resolveString(old, instance);
            List<String> translatedStrings = new ArrayList<>();
            Arrays.stream(newString.split(" ")).forEach(s -> {
                translatedStrings.add(Text.translatable(s).getString());
            });
            newString = String.join(" ", translatedStrings);
        }
        return Text.literal(newString);
    }

    /**
     * Registers a resolver for the given keyword.
     *
     * @param keyWord  the resolver keyword
     * @param resolver the resolver to register
     */
    public static void registerResolver(String keyWord, Resolver resolver) {
        resolverMap.put(keyWord, resolver);
    }

    /**
     * An interface for resolvers, which provide a way to resolve dynamic values contained in square brackets.
     */
    public interface Resolver {
        /**
         * Resolves a double value from the given data string.
         *
         * @param data     the data string to resolve
         * @param instance the module instance for which to resolve the value
         * @return the resolved double value
         */
        double resolveDouble(String data, ItemModule.ModuleInstance instance);

        /**
         * Resolves a string value from the given data string.
         *
         * @param data     the data string to resolve
         * @param instance the module instance for which to resolve the value
         * @return the resolved string value
         */
        String resolveString(String data, ItemModule.ModuleInstance instance);
    }
}
