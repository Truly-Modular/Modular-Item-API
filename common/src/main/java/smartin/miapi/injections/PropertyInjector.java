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
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class PropertyInjector {
    public static final Map<String, TargetSelector> targetSelectors = new HashMap<>();
    public static final InterfaceDispatcher<TargetSelector> targetSelectionDispatcher = InterfaceDispatcher.of(targetSelectors, "type");

    public interface TargetSelector {
        void selectTargetFrom(JsonElement element, JsonInjector injector);
    }
    public interface JsonInjector {
        JsonObject getReplacement(JsonObject json, Map<String, Merger> mergers);
    }
    public interface Merger {
        JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type);
    }

    public static JsonInjector getInjector(JsonObject injectionJson) {
        return (json, mergers) -> getInjectionReplacementFor(injectionJson, json, mergers);
    }

    public static JsonObject getInjectionReplacementFor(JsonObject injectionData, JsonObject toReplace, Map<String, Merger> mergers) {
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

        return replacement;
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
                    Miapi.LOGGER.warn("Invalid path for PropertyInjector found! Token '" + segment + "'(split #" + index + ") in '" + path + "' is invalid: Array index exceeds length of array." +
                            "\nArray: " + array + "\nRoot object: " + root);
                    targetIndex = null;
                } else {
                    currentTarget = array.get(pos);
                    targetIndex = pos;
                }
                targetKey = null;
            } else if (currentTarget instanceof JsonObject obj) {
                JsonElement element = obj.get(segment);
                if (element == null) {
                    Miapi.LOGGER.warn("Invalid path for PropertyInjector found! Token '" + segment + "'(split #" + index + ") in '" + path + " does not match any element in target object.\n" +
                            "Last target object: " + obj + "\nRoot object: " + root);
                    targetKey = null;
                } else {
                    currentTarget = element;
                    targetKey = segment;
                }
                targetIndex = null;
            } else {
                Miapi.LOGGER.warn("Expected path end for PropertyInjector, but instead it continues!" +
                        "\nCurrent segment: " + segment + "\nSegment Index: " + index + "\nWhole path: " + path + "\nCurrent target object: " + currentTarget +
                        "\nRoot object: " + root);
                targetIndex = null;
                targetKey = null;
                break;
            }
        }
        return new TargetHolder(currentTarget, parentObject, targetIndex, targetKey);
    }

    public static class TargetHolder {
        public final JsonElement target;
        public final @Nullable JsonElement parent;
        public final @Nullable Integer index;
        public final @Nullable String key;

        public TargetHolder(JsonElement target, @Nullable JsonElement parent, @Nullable Integer index, @Nullable String key) {
            this.target = target;
            this.parent = parent;
            this.index = index;
            this.key = key;
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
                    Map<String, Merger> mergers = new HashMap<>();
                    module.getKeyedProperties().forEach((property, value) -> {
                        String key = RegistryInventory.moduleProperties.findKey(property);
                        mergers.put(key, property::merge);
                        moduleJson.add(key, value);
                    });

                    JsonObject replacement = injector.getReplacement(moduleJson, mergers);
                    module.getProperties().clear();
                    replacement.asMap().forEach(module.getProperties()::put);
                });
            }
        });
    }
}
