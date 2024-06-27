package smartin.miapi.modules.properties.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.lang.reflect.Type;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

/**
 * Any kind of property of a Module should be implemented here
 */
public interface ModuleProperty {

    /**
     * This function loads and Validates data from the moduleFile
     * Return true if it is valid
     * return false if this shouldn't be loaded
     * throw a detailed error of the data is broken in some way
     * This can be used to cache property data if necessary.
     * if any clientOnly stuff needs implementing {@link ModuleProperty#load(String, JsonElement, boolean)}
     *
     * @param moduleKey the String key of the Module - can be used to cache data
     * @param data      the data associated with this Property
     */
    boolean load(String moduleKey, JsonElement data) throws Exception;


    default boolean load(String moduleKey, JsonElement data, boolean isClient) throws Exception {
        return load(moduleKey, data);
    }


    /**
     * A overwriteable function for merging Property data for dynamic overwrite and merging behaviour
     *
     * @param old     the old propertyData
     * @param toMerge the propertyData to be merged in
     * @param type    type of merging behaviour
     * @return the merged Property data
     */
    default JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        Type typeToken = new TypeToken<List<JsonElement>>() {
        }.getType();
        if (old.isJsonArray() && toMerge.isJsonArray()) {
            List<JsonElement> oldList = Miapi.gson.fromJson(old, typeToken);
            List<JsonElement> newList = Miapi.gson.fromJson(toMerge, typeToken);
            if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
                oldList.addAll(newList);
                return Miapi.gson.toJsonTree(oldList, typeToken);
            } else if (type == MergeType.OVERWRITE) {
                return toMerge;
            }
        } else {
            if (MergeType.EXTEND == type) {
                return old;
            } else {
                return toMerge;
            }
        }
        return old;
    }

    static JsonElement mergeList(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return array;
            }
        }
        return old;
    }

    static JsonElement mergeToList(JsonElement left, JsonElement right) {
        JsonArray returnArray = new JsonArray();
        if (left != null && !left.isJsonNull()) {
            if (left.isJsonArray()) {
                left.getAsJsonArray().forEach(returnArray::add);
            } else {
                returnArray.add(left);
            }
        }
        if (right != null && !right.isJsonNull()) {
            if (right.isJsonArray()) {
                right.getAsJsonArray().forEach(returnArray::add);
            } else {
                returnArray.add(right);
            }
        }
        return right;
    }

    static JsonElement mergeAsMap(JsonElement old, JsonElement toMerge, MergeType mergeType) {
        if (old != null && toMerge != null) {
            Map<String, JsonElement> mapOld = old.getAsJsonObject().asMap();
            Map<String, JsonElement> mapToMerge = toMerge.getAsJsonObject().asMap();
            if (mergeType.equals(MergeType.OVERWRITE)) {
                return toMerge;
            }
            mapOld.putAll(mapToMerge);
            return Miapi.gson.toJsonTree(mapToMerge);
        }
        if (old == null && toMerge != null) {
            return toMerge;
        }
        return old;
    }

    static Map<ModuleProperty, JsonElement> mergeList(Map<ModuleProperty, JsonElement> old, Map<ModuleProperty, JsonElement> toMerge, MergeType type) {
        Map<ModuleProperty, JsonElement> mergedMap = new HashMap<>(old);
        toMerge.forEach((key, json) -> {
            if (mergedMap.containsKey(key)) {
                mergedMap.put(key, mergeList(mergedMap.get(key), json, type));
            } else {
                mergedMap.put(key, json);
            }
        });
        return mergedMap;
    }

    @Nullable
    default JsonElement getJsonElement(ModuleInstance moduleInstance) {
        return moduleInstance.getProperties().get(this);
    }

    @Nullable
    default JsonElement getJsonElement(ItemStack itemStack) {
        return ItemModule.getMergedProperty(itemStack, this);
    }

    static boolean getBoolean(JsonObject object, String element, boolean defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull() && json.isJsonPrimitive()) {
                return json.getAsBoolean();
            }
        }
        return defaultValue;
    }

    static boolean getBoolean(JsonObject object, String element, ModuleInstance moduleInstance, boolean defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                if (json.isJsonPrimitive()) {
                    return json.getAsBoolean();
                }
                return StatResolver.resolveDouble(json, moduleInstance) > 0;
            }
        }
        return defaultValue;
    }

    static double getDouble(JsonObject object, String element, ModuleInstance moduleInstance, double defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                if (moduleInstance == null) {
                    if(json.isJsonPrimitive()){
                        return json.getAsDouble();
                    }
                }
                return StatResolver.resolveDouble(json, moduleInstance);
            }
        }
        return defaultValue;
    }

    static int getInteger(JsonObject object, String element, ModuleInstance moduleInstance, int defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                return (int) StatResolver.resolveDouble(json, moduleInstance);
            }
        }
        return defaultValue;
    }

    static String getString(JsonObject object, String element, ModuleInstance moduleInstance, String defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                return (StatResolver.resolveString(json.getAsString(), moduleInstance));
            }
        }
        return defaultValue;
    }

    static Component getText(JsonObject object, String element, ModuleInstance moduleInstance, Component defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(defaultValue);
            }
        }
        return defaultValue;
    }

    static Component getText(JsonObject object, String element, Component defaultValue) {
        if (object != null) {
            JsonElement json = object.get(element);
            if (json != null && !json.isJsonNull()) {
                return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(defaultValue);
            }
        }
        return defaultValue;
    }

    default Map<ModuleInstance, JsonElement> nonNullJsonElements(ItemStack itemStack) {
        Map<ModuleInstance, JsonElement> maps = new LinkedHashMap<>();
        ItemModule.getModules(itemStack).allSubModules().forEach(moduleInstance -> {
            if (moduleInstance.getProperties().containsKey(this)) {
                maps.put(moduleInstance, moduleInstance.getProperties().get(this));
            }
        });
        return maps;
    }

    @Nullable
    default Tuple<ModuleInstance, JsonElement> highestPriorityJsonElement(ItemStack itemStack) {
        Map<ModuleInstance, JsonElement> maps = new LinkedHashMap<>();
        ModuleInstance moduleInstance = null;
        JsonElement element = null;
        for (ModuleInstance instance : ItemModule.getModules(itemStack).allSubModules()) {
            if (instance.getProperties().containsKey(this)) {
                moduleInstance = instance;
                element = instance.getProperties().get(this);
            }
        }
        if (moduleInstance == null) {
            return null;
        }
        return new Tuple<>(moduleInstance, element);
    }
}
