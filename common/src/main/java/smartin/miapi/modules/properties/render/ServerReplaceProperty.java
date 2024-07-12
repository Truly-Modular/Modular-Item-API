package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class ServerReplaceProperty implements ModuleProperty {
    @Override
    public Object decode(JsonElement element) {
        return null;
    }

    @Override
    public JsonElement encode(Object property) {
        return null;
    }

    @Override
    public Object merge(Object left, Object right, MergeType mergeType) {
        return null;
    }
}
