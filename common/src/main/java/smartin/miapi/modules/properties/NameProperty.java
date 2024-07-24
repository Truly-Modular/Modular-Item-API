package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property is needed to identify Modules
 * THIS IS THE ONLY REQUIRED PROPERTY
 */
public class NameProperty implements ModuleProperty {
    public static final String KEY = "name";


    @Override
    public Object decode(JsonElement element) {
        return element.getAsString();
    }

    @Override
    public JsonElement encode(Object property) {
        return new JsonObject();
    }

    @Override
    public Object merge(Object left, Object right, MergeType mergeType) {
        return null;
    }
}
