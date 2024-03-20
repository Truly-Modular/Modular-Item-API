package smartin.miapi.injectors;

import com.google.gson.*;
import com.redpxnda.nucleus.util.InterfaceDispatcher;
import org.jetbrains.annotations.Nullable;
import org.mariuszgromada.math.mxparser.Expression;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertySubstitution {
    public static int injectorsCount = 0;
    public static final Map<String, TargetSelector> targetSelectors = new HashMap<>();
    public static final InterfaceDispatcher<TargetSelector> targetSelectionDispatcher = InterfaceDispatcher.of(targetSelectors, "type");
    public static final Map<String, ValueResolver> valueResolvers = new HashMap<>();
    public static final InterfaceDispatcher<ValueResolver> valueResolverDispatcher = InterfaceDispatcher.of(valueResolvers, "mode");

    public interface TargetSelector {
        void triggerTargetFrom(JsonElement element, JsonInjector injector);
    }

    public interface JsonInjector {
        JsonObject getReplacement(JsonObject json);
    }

    public interface ValueResolver {
        JsonElement resolve(JsonElement input, JsonElement originalTarget, Map<String, JsonElement> storedValues);
    }

    public static JsonInjector getInjector(JsonObject injectionJson) {
        return json -> getInjectionReplacementFor(injectionJson, json);
    }

    public static JsonObject getInjectionReplacementFor(JsonObject injectionData, JsonObject toReplace) {
        JsonObject replacement = toReplace.deepCopy();
        Map<String, JsonElement> storedValues = new HashMap<>();
        if (injectionData.get("read") instanceof JsonObject reader) {
            reader.asMap().forEach((name, data) -> {
                storedValues.put(name, getValueFromLocator(toReplace, data.getAsString()).target);
            });
        }

        if (injectionData.get("remove") instanceof JsonArray array) {
            array.forEach(removeLocation -> {
                TargetHolder holder = getValueFromLocator(replacement, removeLocation.getAsString());
                if (holder.parent instanceof JsonObject object && holder.key != null) {
                    JsonElement removed = object.remove(holder.key);
                    if (removed == null)
                        Miapi.LOGGER.warn("Failed to remove key '" + removeLocation + "' (object fail) for PropertySubstitution!\nOriginal object: " + toReplace);
                } else if (holder.parent instanceof JsonArray arr) {
                    boolean bl = arr.remove(holder.target);
                    if (!bl)
                        Miapi.LOGGER.warn("Failed to remove key '" + removeLocation + "' (array fail) for PropertySubstitution!\nOriginal object: " + toReplace);
                }
            });
        }

        if (injectionData.get("write") instanceof JsonObject outer) {
            outer.asMap().forEach((key, element) -> {
                if (!(element instanceof JsonObject object)) {
                    Miapi.LOGGER.warn("Write field for PropertySubstitution contains a non JSON object element: '" + element + "'");
                    return;
                }

                TargetHolder holder = getValueFromLocator(replacement, key);
                JsonElement targetReplacement = valueResolverDispatcher.dispatcher().resolve(object, holder.target, storedValues);
                holder.set(targetReplacement);
            });
        }

        return replacement;
    }

    public static MergeType getMergeType(String stringRepresentation) {
        return switch (stringRepresentation) {
            case "smart" -> MergeType.SMART;
            case "extend" -> MergeType.EXTEND;
            default -> MergeType.OVERWRITE;
        };
    }

    /**
     * @return a target representing the targeted value its (nullable) parent, and the (nullable) index it was in(only if parent was array).
     */
    public static TargetHolder getValueFromLocator(JsonObject root, String path) {
        JsonElement currentTarget = root;
        JsonElement parentObject = null;
        Integer targetIndex = null;
        String targetKey = null;
        String[] segments = path.split("(?<!~)\\.(?!~)");
        int index = 0;
        for (String segment : segments) {
            parentObject = currentTarget;
            index++;
            segment = segment.replace("~~", "");

            if (segment.matches("\\[\\d+]$") && currentTarget instanceof JsonArray array) {
                int pos = Integer.parseInt(segment.substring(1, segment.length() - 1));
                if (pos >= array.size() || pos < 0) {
                    Miapi.LOGGER.error("Invalid path for PropertySubstitution found! Token '" + segment + "'(split #" + index + ") in '" + path + "' is invalid: Array index exceeds length of array." +
                            "\nArray: " + array + "\nRoot object: " + root);
                    throw new RuntimeException("Failed to parse PropertySubstitution! See above error.");
                } else {
                    currentTarget = array.get(pos);
                    targetIndex = pos;
                }
                targetKey = null;
            } else if (currentTarget instanceof JsonObject obj) {
                JsonElement element = obj.get(segment);
                if (element == null) {
                    Miapi.LOGGER.error("Invalid path for PropertySubstitution found! Token '" + segment + "'(split #" + index + ") in '" + path + " does not match any element in target object.\n" +
                            "Last target object: " + obj + "\nRoot object: " + root);
                    throw new RuntimeException("Failed to parse PropertySubstitution! See above error.");
                } else {
                    currentTarget = element;
                    targetKey = segment;
                }
                targetIndex = null;
            } else {
                Miapi.LOGGER.warn("Expected path end for PropertySubstitution, but instead it continues! Cutting off early." +
                        "\nCurrent segment: " + segment + "\nSegment Index: " + index + "\nWhole path: " + path + "\nCurrent target object: " + currentTarget +
                        "\nRoot object: " + root);
                break;
            }
        }
        return new TargetHolder(currentTarget, parentObject, targetIndex, targetKey);
    }

    public record TargetHolder(JsonElement target, @Nullable JsonElement parent, @Nullable Integer index,
                               @Nullable String key) {
        public void set(JsonElement newValue) {
            if (parent instanceof JsonObject object && key != null) {
                object.add(key, newValue);
            } else if (parent instanceof JsonArray array && index != null) {
                array.set(index, newValue);
            }
        }
    }

    static {
        valueResolvers.put("exact", (input, originalTarget, storedValues) -> { // exact mode uses a specific inputted json element
            if (!(input instanceof JsonObject object)) return originalTarget; // shouldn't ever return here
            JsonElement val = object.get("value");
            if (val == null) {
                Miapi.LOGGER.warn("Failed to found key 'value' for exact value resolver in a PropertySubstitution! Returning original instead.");
                return originalTarget;
            }
            return val;
        });
        valueResolvers.put("append", (input, originalTarget, storedValues) -> { // append mode adds a specific element to an array
            if (!(input instanceof JsonObject object)) return originalTarget; // shouldn't ever return here
            if (originalTarget instanceof JsonArray array) {
                JsonElement val = object.get("value");
                if (val == null) {
                    Miapi.LOGGER.warn("Failed to found key 'value' for append value resolver in a PropertySubstitution! Returning original instead.");
                    return originalTarget;
                }

                Consumer<JsonArray> adder;
                if (val instanceof JsonArray valArray && !object.has("insert_as_array")) {
                    adder = arr -> arr.addAll(valArray);
                } else adder = arr -> arr.add(val);

                JsonArray newArray = array.deepCopy();
                adder.accept(newArray);
                return newArray;
            }
            Miapi.LOGGER.warn("Target for append value resolver in a PropertySubstitution is not an array nor a string! Returning original value.");
            return originalTarget;
        });
        valueResolvers.put("calculate", (input, originalTarget, storedValues) -> { // calculate mode calculates string expressions using stored variables
            if (!(input instanceof JsonObject object)) return originalTarget; // shouldn't ever return here
            JsonElement val = object.get("value");
            if (!(val instanceof JsonPrimitive prim) || !prim.isString()) {
                Miapi.LOGGER.warn("Key 'value' for calculate value resolver in a PropertySubstitution is either missing or not a string! Returning original instead.");
                return originalTarget;
            }
            String expression = prim.getAsString();
            Pattern pattern = Pattern.compile("\\[(.*?)]");
            Matcher matcher = pattern.matcher(expression);
            while (matcher.find()) {
                String targetVariable = matcher.group(1);
                JsonElement value = storedValues.get(targetVariable);
                if (!(value instanceof JsonPrimitive)) {
                    Miapi.LOGGER.error("Target value for stored variable '" + targetVariable + "' referenced in calulcate value resolver in a PropertySubstitution is not a JSON primitive!");
                    throw new RuntimeException();
                }
                expression = expression.replace("[" + targetVariable + "]", value.getAsString());
            }

            Expression ex = new Expression(expression);
            return new JsonPrimitive(ex.calculate());
        });
        valueResolvers.put("replace", (input, originalTarget, storedValues) -> { // replace mode replaces variables in strings using the stored variables
            if (!(input instanceof JsonObject object)) return originalTarget; // shouldn't ever return here
            JsonElement val = object.get("value");
            if (!(val instanceof JsonPrimitive prim) || !prim.isString()) {
                Miapi.LOGGER.warn("Key 'value' for replace value resolver in a PropertySubstitution is either missing or not a string! Returning original instead.");
                return originalTarget;
            }
            String string = prim.getAsString();
            Pattern pattern = Pattern.compile("\\[(.*?)]");
            Matcher matcher = pattern.matcher(string);
            while (matcher.find()) {
                String targetVar = matcher.group(1);
                JsonElement value = storedValues.get(targetVar);
                string = string.replace("[" + targetVar + "]", value.getAsString());
            }

            return new JsonPrimitive(string);
        });

        targetSelectors.put("modules", (root, injector) -> {
            if (!(root instanceof JsonObject object))
                throw new JsonParseException("Failed to load Miapi module injection! Not a json object -> " + root);

            List<ItemModule> modules = new ArrayList<>();

            if (object.get("keys") instanceof JsonArray array) {
                array.forEach(moduleKey -> {
                    ItemModule module = ItemModule.moduleRegistry.get(moduleKey.getAsString());
                    if (module == null) return;
                    modules.add(module);
                });
            }
            if (object.get("tags") instanceof JsonArray array) {
                array.forEach(tag -> {
                    modules.addAll(TagProperty.getModulesWithTag(tag.getAsString()));
                });
            }
            if (object.get("regex") instanceof JsonPrimitive prim) {
                ItemModule.moduleRegistry.getFlatMap().forEach((key, module) -> {
                    if (key.matches(prim.getAsString())) modules.add(module);
                });
            }
            if (object.get("blacklist") instanceof JsonArray array) {
                array.forEach(moduleKey -> {
                    ItemModule module = ItemModule.moduleRegistry.get(moduleKey.getAsString());
                    if (module == null) return;
                    modules.remove(module);
                });
            }

            modules.forEach(module -> {
                JsonObject moduleJson = new JsonObject();
                module.getProperties().forEach(moduleJson::add);

                JsonObject replacement = injector.getReplacement(moduleJson);
                module.getProperties().clear();
                replacement.asMap().forEach((key, value) -> {
                    ModuleProperty property = RegistryInventory.moduleProperties.get(key);
                    if (property == null) return;

                    try {
                        property.load(module.getName(), value, Environment.isClient());
                    } catch (Exception ex) {
                        Miapi.LOGGER.error("Property load error whilst loading PropertySubstitution injection data for a module!");
                        throw new RuntimeException(ex);
                    }

                    module.getProperties().put(key, value);
                });
            });
        });
    }
}
