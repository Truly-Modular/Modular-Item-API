package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class ServerReplaceProperty implements ModuleProperty {
    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}
