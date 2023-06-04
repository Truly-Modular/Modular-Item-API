package smartin.miapi.item.modular;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import org.mariuszgromada.math.mxparser.Expression;
import smartin.miapi.modules.ItemModule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * A map of resolvers, keyed by resolver keyword.
     */
    private static final Map<String, Resolver> resolverMap = new HashMap<>();

    /**
     * Resolves all string values contained in square brackets in the input string.
     *
     * @param raw      the input string
     * @param instance the module instance for which to resolve values
     * @return the resolved string
     */
    public static String resolveString(String raw, ItemModule.ModuleInstance instance) {
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
                    String resolvedData = resolver.resolveString(resolverData, instance);
                    resolved = resolved.replace("[" + match + "]", resolvedData);
                } else {
                    //resolved = resolved.replace("[" + match + "]", "");
                }
            }
        }
        return resolved;
    }

    /**
     * Resolves all double values contained in square brackets in the input string, and evaluates the resulting expression.
     *
     * @param raw      the input string
     * @param instance the module instance for which to resolve values
     * @return the evaluated result
     */
    public static double resolveDouble(String raw, ItemModule.ModuleInstance instance) {
        try{
            return Double.parseDouble(raw);
        }
        catch (Exception exception){

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
