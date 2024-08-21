package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class ServerReplaceProperty implements ModuleProperty {
    @Override
    public Object decode(JsonElement element) {
        return new Object();
    }

    @Override
    public JsonElement encode(Object property) {
        return new JsonObject();
    }

    @Override
    public Object merge(Object left, Object right, MergeType mergeType) {
        return new Object();
    }
}
