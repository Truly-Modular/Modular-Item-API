package smartin.miapi.modules.properties.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import smartin.miapi.Miapi;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

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
     *
     * @param moduleKey the String key of the Module - can be used to cache data
     * @param data      the data associated with this Property
     */
    boolean load(String moduleKey, JsonElement data) throws Exception;

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
        List<JsonElement> oldList = Miapi.gson.fromJson(old, typeToken);
        List<JsonElement> newList = Miapi.gson.fromJson(toMerge, typeToken);
        if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
            oldList.addAll(newList);
            return Miapi.gson.toJsonTree(oldList, typeToken);
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
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
}
