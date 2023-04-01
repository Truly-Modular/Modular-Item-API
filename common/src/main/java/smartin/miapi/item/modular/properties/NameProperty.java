package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;

public class NameProperty implements ModuleProperty {
    public static final String key = "name";

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        return true;
    }
}
