package smartin.miapi.item.modular.properties.crafting;

import com.google.gson.JsonElement;
import smartin.miapi.item.modular.properties.ModuleProperty;

public class CraftingProperty implements ModuleProperty {
    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }
}
