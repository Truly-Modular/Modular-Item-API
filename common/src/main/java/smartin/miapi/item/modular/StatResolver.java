package smartin.miapi.item.modular;

import net.minecraft.text.Text;
import org.mariuszgromada.math.mxparser.Expression;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatResolver {

    private static final Map<String, Resolver> resolverMap = new HashMap<>();

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

    public static double resolveDouble(String raw, ItemModule.ModuleInstance instance) {
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

    public static Text translateAndResolve(String raw, ItemModule.ModuleInstance instance) {
        String old = "";
        String newString = raw;
        for(int i = 0;i<100 && !old.equals(newString);i++ ){
            old = newString;
            newString = resolveString(old, instance);
            newString = Text.translatable(newString).getString();
        }
        return Text.literal(newString);
    }

    private static String getLastVariableName(String variableName) {
        return variableName.substring(variableName.indexOf(".") + 1);
    }


    public static void registerResolver(String keyWord, Resolver resolver) {
        resolverMap.put(keyWord, resolver);
    }

    public interface Resolver {
        double resolveDouble(String data, ItemModule.ModuleInstance instance);

        String resolveString(String data, ItemModule.ModuleInstance instance);
    }
}
