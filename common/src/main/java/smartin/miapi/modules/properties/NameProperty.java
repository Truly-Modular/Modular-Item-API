package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property is needed to identify Modules
 * THIS IS THE ONLY REQUIRED PROPERTY
 */
public class NameProperty implements ModuleProperty {
    public static final String KEY = "name";

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case EXTEND -> {
                return old;
            }
            case SMART, OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }
}
