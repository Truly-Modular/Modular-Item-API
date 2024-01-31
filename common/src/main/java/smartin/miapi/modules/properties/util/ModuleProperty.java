package smartin.miapi.modules.properties.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import smartin.miapi.Miapi;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if(old.isJsonArray() && toMerge.isJsonArray()){
            List<JsonElement> oldList = Miapi.gson.fromJson(old, typeToken);
            List<JsonElement> newList = Miapi.gson.fromJson(toMerge, typeToken);
            if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
                oldList.addAll(newList);
                return Miapi.gson.toJsonTree(oldList, typeToken);
            } else if (type == MergeType.OVERWRITE) {
                return toMerge;
            }
        }
        else{
            if(MergeType.EXTEND == type){
                return old;
            }
            else{
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

    static JsonElement mergeToList(JsonElement left,JsonElement right){
        JsonArray returnArray = new JsonArray();
        if(left!=null && !left.isJsonNull()){
            if(left.isJsonArray()){
                left.getAsJsonArray().forEach(returnArray::add);
            }
            else{
                returnArray.add(left);
            }
        }
        if(right!=null && !right.isJsonNull()){
            if(right.isJsonArray()){
                right.getAsJsonArray().forEach(returnArray::add);
            }
            else{
                returnArray.add(right);
            }
        }
        return right;
    }

    static Map<ModuleProperty,JsonElement> mergeList(Map<ModuleProperty,JsonElement> old, Map<ModuleProperty,JsonElement> toMerge, MergeType type) {
        Map<ModuleProperty,JsonElement> mergedMap = new HashMap<>(old);
        toMerge.forEach((key,json)->{
            if(mergedMap.containsKey(key)){
                mergedMap.put(key,mergeList(mergedMap.get(key),json,type));
            }
            else{
                mergedMap.put(key,json);
            }
        });
        return mergedMap;
    }
}
