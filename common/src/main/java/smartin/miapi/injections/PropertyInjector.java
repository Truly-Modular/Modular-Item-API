package smartin.miapi.injections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.redpxnda.nucleus.datapack.codec.InterfaceDispatcher;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class PropertyInjector {
    public static final Map<String, TargetSelector> targetSelectors = new HashMap<>();
    public static final InterfaceDispatcher<TargetSelector> targetSelectionDispatcher = InterfaceDispatcher.of(targetSelectors, "type");
    public static final Map<String, ValueResolver> valueResolvers = new HashMap<>();
    public static final InterfaceDispatcher<ValueResolver> valueResolverDispatcher = InterfaceDispatcher.of(valueResolvers, "mode");

    public interface TargetSelector {
        void selectTargetFrom(JsonElement element, JsonInjector injector);
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
                    if (removed == null) Miapi.LOGGER.warn("Failed to remove key '" + removeLocation + "' (object fail) for PropertyInjector!\nOriginal object: " + toReplace);
                } else if (holder.parent instanceof JsonArray arr) {
                    boolean bl = arr.remove(holder.target);
                    if (!bl) Miapi.LOGGER.warn("Failed to remove key '" + removeLocation + "' (array fail) for PropertyInjector!\nOriginal object: " + toReplace);
                }
            });
        }

        if (injectionData.get("write") instanceof JsonObject outer) {
            outer.asMap().forEach((key, element) -> {
                if (!(element instanceof JsonObject object)) {
                    Miapi.LOGGER.warn("Write field for PropertyInjector contains a non JSON object element: '" + element + "'");
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
                    Miapi.LOGGER.error("Invalid path for PropertyInjector found! Token '" + segment + "'(split #" + index + ") in '" + path + "' is invalid: Array index exceeds length of array." +
                            "\nArray: " + array + "\nRoot object: " + root);
                    throw new RuntimeException("Failed to parse PropertyInjector! See above error.");
                } else {
                    currentTarget = array.get(pos);
                    targetIndex = pos;
                }
                targetKey = null;
            } else if (currentTarget instanceof JsonObject obj) {
                JsonElement element = obj.get(segment);
                if (element == null) {
                    Miapi.LOGGER.error("Invalid path for PropertyInjector found! Token '" + segment + "'(split #" + index + ") in '" + path + " does not match any element in target object.\n" +
                            "Last target object: " + obj + "\nRoot object: " + root);
                    throw new RuntimeException("Failed to parse PropertyInjector! See above error.");
                } else {
                    currentTarget = element;
                    targetKey = segment;
                }
                targetIndex = null;
            } else {
                Miapi.LOGGER.warn("Expected path end for PropertyInjector, but instead it continues! Cutting off early." +
                        "\nCurrent segment: " + segment + "\nSegment Index: " + index + "\nWhole path: " + path + "\nCurrent target object: " + currentTarget +
                        "\nRoot object: " + root);
                break;
            }
        }
        return new TargetHolder(currentTarget, parentObject, targetIndex, targetKey);
    }

    public record TargetHolder(JsonElement target, @Nullable JsonElement parent, @Nullable Integer index, @Nullable String key) {
        public void set(JsonElement newValue) {
            if (parent instanceof JsonObject object && key != null) {
                object.add(key, newValue);
            } else if (parent instanceof JsonArray array && index != null) {
                array.set(index, newValue);
            }
        }
    }

    static {
        targetSelectors.put("modules", (root, injector) -> {
            if (!(root instanceof JsonObject object)) throw new JsonParseException("Failed to load Miapi module injection! Not a json object -> " + root);

            if (object.get("keys") instanceof JsonArray array) {
                array.forEach(e -> {
                    ItemModule module = ItemModule.moduleRegistry.get(e.getAsString());
                    if (module == null) return;
                    JsonObject moduleJson = new JsonObject();
                    module.getProperties().forEach(moduleJson::add);

                    JsonObject replacement = injector.getReplacement(moduleJson);
                    module.getProperties().clear();
                    replacement.asMap().forEach((key, value) -> {
                        ModuleProperty property = RegistryInventory.moduleProperties.get(key);
                        if (property == null) return;

                        try {
                            property.load(module.getName(), value);
                        } catch (Exception ex) {
                            Miapi.LOGGER.error("Exception whilst loading PropertyInject injection data for a module!");
                            throw new RuntimeException(ex);
                        }

                        module.getProperties().put(key, value);
                    });
                });
            }
        });
    }
}
